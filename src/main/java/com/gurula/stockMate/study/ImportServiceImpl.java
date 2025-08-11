package com.gurula.stockMate.study;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.layout.Layout;
import com.gurula.stockMate.layout.LayoutRepository;
import com.gurula.stockMate.layout.dto.LayoutDTO;
import com.gurula.stockMate.news.News;
import com.gurula.stockMate.news.NewsRepository;
import com.gurula.stockMate.note.Note;
import com.gurula.stockMate.note.dto.NoteDTO;
import com.gurula.stockMate.note.NoteRepository;
import java.util.Optional;

import com.gurula.stockMate.symbol.Symbol;
import com.gurula.stockMate.symbol.SymbolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImportServiceImpl implements ImportService {
    private final StudyNewsRepository studyNewsRepository;
    private final NewsRepository newsRepository;
    private final StudyRepository studyRepository;
    private final NoteRepository noteRepository;
    private final StudyNoteRepository studyNoteRepository;
    private final LayoutRepository layoutRepository;
    private final StudyLayoutRepository studyLayoutRepository;
    private final StudyContentItemRepository studyContentItemRepository;
    private final StudyNoteVersionRepository studyNoteVersionRepository;
    private final StudyLayoutVersionRepository studyLayoutVersionRepository;
    private final SymbolRepository symbolRepository;

    public ImportServiceImpl(StudyNewsRepository studyNewsRepository, NewsRepository newsRepository, StudyRepository studyRepository, NoteRepository noteRepository, StudyNoteRepository studyNoteRepository, LayoutRepository layoutRepository, StudyLayoutRepository studyLayoutRepository, StudyContentItemRepository studyContentItemRepository, StudyNoteVersionRepository studyNoteVersionRepository, StudyLayoutVersionRepository studyLayoutVersionRepository,
                             SymbolRepository symbolRepository) {
        this.studyNewsRepository = studyNewsRepository;
        this.newsRepository = newsRepository;
        this.studyRepository = studyRepository;
        this.noteRepository = noteRepository;
        this.studyNoteRepository = studyNoteRepository;
        this.layoutRepository = layoutRepository;
        this.studyLayoutRepository = studyLayoutRepository;
        this.studyContentItemRepository = studyContentItemRepository;
        this.studyNoteVersionRepository = studyNoteVersionRepository;
        this.studyLayoutVersionRepository = studyLayoutVersionRepository;
        this.symbolRepository = symbolRepository;
    }

    @Override
    @Transactional
    public Result<StudyContentDTO, String> importNewsToStudy(ImportDTO importDTO, String memberId) {
        String studyId = importDTO.getSelfId();
        String newsId = importDTO.getContentId();

        // 驗證 Study 是否存在且為該 member 所有
        Optional<Study> studyOptional = studyRepository.findByIdAndMemberId(studyId, memberId);
        if (studyOptional.isEmpty()) {
            return Result.err("Study not found or not owned by member for ID: " + studyId);
        }

        // 驗證 NewsId 是否合法
        if (newsId == null || newsId.isEmpty()) {
            return Result.err("News ID cannot be null or empty.");
        }

        Optional<News> validNews = newsRepository.findByIdAndMemberId(newsId, memberId);
        if (validNews.isEmpty()) {
            return Result.err("Invalid or unauthorized News ID found: " + newsId);
        }

        final News news = validNews.get();

        // 檢查是否已經關聯過
        boolean studyNewsExists = studyNewsRepository.findByStudyIdAndNewsId(studyId, newsId).isPresent();
        boolean studyContentItemExists = studyContentItemRepository.findByStudyIdAndContentTypeAndContentId(
                studyId, ContentType.NEWS, newsId
        ).isPresent();

        // 取得排序序號
        int nextSortOrder = 0;
        Optional<StudyContentItem> lastItem = studyContentItemRepository.findTopByStudyIdOrderBySortOrderDesc(studyId);
        if (lastItem.isPresent()) {
            nextSortOrder = lastItem.get().getSortOrder() + 1;
        }

        boolean changesMade = false;

        // 加入 StudyNews 關聯
        if (!studyNewsExists) {
            StudyNews studyNews = new StudyNews();
            studyNews.setStudyId(studyId);
            studyNews.setNewsId(newsId);
            studyNewsRepository.save(studyNews);
            changesMade = true;
        }

        // 加入 StudyContentItem 排序關聯
        StudyContentItem studyContentItem = null;
        if (!studyContentItemExists) {
            studyContentItem = new StudyContentItem(studyId, ContentType.NEWS, newsId, nextSortOrder);
            studyContentItemRepository.save(studyContentItem);
            changesMade = true;
        }

        if (changesMade) {
            StudyContentDTO dto = new StudyContentDTO();
            dto.setTitle(news.getTitle());
            dto.setType(ContentType.NEWS);
            dto.setId(studyContentItem.getContentId());
            dto.setData(news.toDto());
            return Result.ok(dto);
        } else {
            return Result.ok(new StudyContentDTO());
        }
    }

    @Override
    @Transactional
    public Result<StudyContentDTO, String> importNotesToStudy(ImportDTO importDTO, String memberId) {
        String studyId = importDTO.getSelfId();
        String noteId = importDTO.getContentId();
        boolean syncEnabled = importDTO.isSyncEnabled();

        // 驗證 Study 是否存在且屬於該 member
        Optional<Study> studyOptional = studyRepository.findByIdAndMemberId(studyId, memberId);
        if (studyOptional.isEmpty()) {
            return Result.err("Study not found or not owned by member for ID: " + studyId);
        }

        // 驗證 NoteId 是否有效
        if (noteId == null || noteId.isEmpty()) {
            return Result.err("Note ID cannot be null or empty.");
        }

        Optional<Note> validNote = noteRepository.findByIdAndMemberId(noteId, memberId);
        if (validNote.isEmpty()) {
            return Result.err("Invalid or unauthorized Note ID found: " + noteId);
        }

        Note originalNote = validNote.get();

        // 檢查 Note 是否已關聯
        boolean studyNoteExists = studyNoteRepository.findByStudyIdAndNoteId(studyId, noteId).isPresent();
        boolean studyContentItemExists = studyContentItemRepository
                .findByStudyIdAndContentTypeAndContentId(studyId, ContentType.NOTE, noteId)
                .isPresent();

        // 取得下一個排序
        int nextSortOrder = 0;
        Optional<StudyContentItem> lastItem = studyContentItemRepository.findTopByStudyIdOrderBySortOrderDesc(studyId);
        if (lastItem.isPresent()) {
            nextSortOrder = lastItem.get().getSortOrder() + 1;
        }

        boolean changesMade = false;

        StudyNoteVersion initialVersion = null;
        // 建立 StudyNote 關聯
        if (!studyNoteExists) {
            StudyNote studyNote = new StudyNote();
            studyNote.setStudyId(studyId);
            studyNote.setNoteId(noteId);
            studyNote.setSyncEnabled(syncEnabled);
            studyNote = studyNoteRepository.save(studyNote);
            changesMade = true;

            // 若不同步，則儲存初始版本
            if (!syncEnabled) {
                initialVersion = new StudyNoteVersion();
                initialVersion.setStudyNoteId(studyNote.getId());
                initialVersion.setTitle(originalNote.getTitle());
                initialVersion.setContent(originalNote.getContent());
                initialVersion.setCreatedAt(System.currentTimeMillis());
                initialVersion.setVersionType(VersionType.SNAPSHOT);
                initialVersion.setMemberId(memberId);

                initialVersion = studyNoteVersionRepository.save(initialVersion);
                studyNote.setCurrentVersionId(initialVersion.getId());
                studyNoteRepository.save(studyNote);
            }
        }

        // 建立 StudyContentItem 排序關聯
        StudyContentItem studyContentItem = null;
        if (!studyContentItemExists) {
            String contentId = syncEnabled ? noteId : initialVersion.getId();
            studyContentItem = new StudyContentItem(
                    studyId, ContentType.NOTE, contentId, nextSortOrder
            );
            studyContentItemRepository.save(studyContentItem);
            changesMade = true;
        }

        if (changesMade) {
            StudyContentDTO dto = new StudyContentDTO();
            dto.setTitle(originalNote.getTitle());
            dto.setType(ContentType.NOTE);
            dto.setId(studyContentItem.getContentId());
            if (syncEnabled) {
                final NoteDTO noteDTO = originalNote.toDto();
                noteDTO.setVersionType(VersionType.SYNC);
                dto.setData(noteDTO);
            } else {
                dto.setData(initialVersion.toDto());
            }
            return Result.ok(dto);
        } else {
            return Result.ok(new StudyContentDTO());
        }
    }

    @Override
    @Transactional
    public Result<StudyContentDTO, String> importLayoutsToStudy(ImportDTO importDTO, String memberId) {
        String studyId = importDTO.getSelfId();
        String layoutId = importDTO.getContentId();
        boolean syncEnabled = importDTO.isSyncEnabled();

        // 驗證 Study 是否存在且屬於該 member
        Optional<Study> studyOptional = studyRepository.findByIdAndMemberId(studyId, memberId);
        if (studyOptional.isEmpty()) {
            return Result.err("Study not found or not owned by member for ID: " + studyId);
        }

        // 驗證 LayoutId 是否有效
        if (layoutId == null || layoutId.isEmpty()) {
            return Result.err("LayoutId cannot be null or empty.");
        }

        Optional<Layout> validLayout = layoutRepository.findByIdAndMemberId(layoutId, memberId);
        if (validLayout.isEmpty()) {
            return Result.err("Invalid or unauthorized LayoutId found: " + layoutId);
        }

        Layout originalLayout = validLayout.get();

        // 檢查是否已關聯
        boolean studyLayoutExists = studyLayoutRepository.findByStudyIdAndLayoutId(studyId, layoutId).isPresent();
        boolean studyContentItemExists = studyContentItemRepository
                .findByStudyIdAndContentTypeAndContentId(studyId, ContentType.LAYOUT, layoutId)
                .isPresent();

        // 取得下一個排序序號
        int nextSortOrder = 0;
        Optional<StudyContentItem> lastItem = studyContentItemRepository.findTopByStudyIdOrderBySortOrderDesc(studyId);
        if (lastItem.isPresent()) {
            nextSortOrder = lastItem.get().getSortOrder() + 1;
        }

        boolean changesMade = false;

        StudyLayoutVersion initialVersion = null;
        // 建立 StudyLayout 關聯
        if (!studyLayoutExists) {
            StudyLayout studyLayout = new StudyLayout();
            studyLayout.setStudyId(studyId);
            studyLayout.setLayoutId(layoutId);
            studyLayout = studyLayoutRepository.save(studyLayout);
            changesMade = true;

            // 若不同步，建立初始版本快照
            if (!syncEnabled) {
                initialVersion = new StudyLayoutVersion();
                initialVersion.setStudyLayoutId(studyLayout.getId());
                initialVersion.setName(originalLayout.getName());
                initialVersion.setDesc(originalLayout.getDesc());
                initialVersion.setInterval(originalLayout.getInterval());
                initialVersion.setUserSettings(originalLayout.getUserSettings());
                initialVersion.setCreatedAt(System.currentTimeMillis());
                initialVersion.setVersionType(VersionType.SNAPSHOT);
                initialVersion.setMemberId(memberId);
                initialVersion.setSymbolId(originalLayout.getSymbolId());

                initialVersion = studyLayoutVersionRepository.save(initialVersion);
                studyLayout.setCurrentVersionId(initialVersion.getId());
                studyLayoutRepository.save(studyLayout); // 更新版本 ID
            }
        }

        // 建立 StudyContentItem 排序
        StudyContentItem studyContentItem = null;
        if (!studyContentItemExists) {
            String contentId = syncEnabled ? layoutId : initialVersion.getId();
            studyContentItem = new StudyContentItem(
                    studyId, ContentType.LAYOUT, contentId, nextSortOrder
            );
            studyContentItemRepository.save(studyContentItem);
            changesMade = true;
        }

        if (changesMade) {
            StudyContentDTO dto = new StudyContentDTO();
            dto.setTitle(originalLayout.getName());
            dto.setType(ContentType.LAYOUT);
            dto.setId(studyContentItem.getContentId());
            final Symbol symbol = symbolRepository.findById(originalLayout.getSymbolId()).get();
            final LayoutDTO layoutDTO;
            if (syncEnabled) {
                layoutDTO = originalLayout.toDto();
                layoutDTO.setSymbol(symbol.getSymbol());
                layoutDTO.setVersionType(VersionType.SYNC);
            } else {
                layoutDTO = initialVersion.toDto();
                layoutDTO.setSymbol(symbol.getSymbol());
            }
            dto.setData(layoutDTO);

            return Result.ok(dto);
        } else {
            return Result.ok(new StudyContentDTO());
        }
    }
}
