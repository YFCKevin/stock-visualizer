package com.gurula.stockMate.study;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.layout.Layout;
import com.gurula.stockMate.layout.LayoutRepository;
import com.gurula.stockMate.news.News;
import com.gurula.stockMate.news.NewsRepository;
import com.gurula.stockMate.note.Note;
import com.gurula.stockMate.note.NoteRepository;
import java.util.Optional;
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

    public ImportServiceImpl(StudyNewsRepository studyNewsRepository, NewsRepository newsRepository, StudyRepository studyRepository, NoteRepository noteRepository, StudyNoteRepository studyNoteRepository, LayoutRepository layoutRepository, StudyLayoutRepository studyLayoutRepository, StudyContentItemRepository studyContentItemRepository, StudyNoteVersionRepository studyNoteVersionRepository, StudyLayoutVersionRepository studyLayoutVersionRepository) {
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
    }

    @Override
    @Transactional
    public Result<String, String> importNewsToStudy(ImportDTO importDTO, String memberId) {
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
        if (!studyContentItemExists) {
            StudyContentItem studyContentItem = new StudyContentItem(studyId, ContentType.NEWS, newsId, nextSortOrder);
            studyContentItemRepository.save(studyContentItem);
            changesMade = true;
        }

        if (changesMade) {
            return Result.ok("News '" + newsId + "' associated and ordered within study successfully.");
        } else {
            return Result.ok("News '" + newsId + "' was already associated and ordered within study. No new changes made.");
        }
    }

    @Override
    @Transactional
    public Result<String, String> importNotesToStudy(ImportDTO importDTO, String memberId) {
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
                StudyNoteVersion initialVersion = new StudyNoteVersion();
                initialVersion.setStudyNoteId(studyNote.getId());
                initialVersion.setTitle(originalNote.getTitle());
                initialVersion.setContent(originalNote.getContent());
                initialVersion.setCreatedAt(System.currentTimeMillis());
                initialVersion.setVersionType(VersionType.SNAPSHOT);
                initialVersion.setMemberId(memberId);

                initialVersion = studyNoteVersionRepository.save(initialVersion);
                studyNote.setCurrentVersionId(initialVersion.getId());
                studyNoteRepository.save(studyNote); // 更新版本 ID
            }
        }

        // 建立 StudyContentItem 排序關聯
        if (!studyContentItemExists) {
            StudyContentItem studyContentItem = new StudyContentItem(
                    studyId, ContentType.NOTE, noteId, nextSortOrder
            );
            studyContentItemRepository.save(studyContentItem);
            changesMade = true;
        }

        if (changesMade) {
            return Result.ok("Note '" + noteId + "' associated and ordered within study successfully.");
        } else {
            return Result.ok("Note '" + noteId + "' was already associated and ordered within study. No new changes made.");
        }
    }

    @Override
    @Transactional
    public Result<String, String> importLayoutsToStudy(ImportDTO importDTO, String memberId) {
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

        // 建立 StudyLayout 關聯
        if (!studyLayoutExists) {
            StudyLayout studyLayout = new StudyLayout();
            studyLayout.setStudyId(studyId);
            studyLayout.setLayoutId(layoutId);
            studyLayout = studyLayoutRepository.save(studyLayout);
            changesMade = true;

            // 若不同步，建立初始版本快照
            if (!syncEnabled) {
                StudyLayoutVersion initialVersion = new StudyLayoutVersion();
                initialVersion.setStudyLayoutId(studyLayout.getId());
                initialVersion.setName(originalLayout.getName());
                initialVersion.setDesc(originalLayout.getDesc());
                initialVersion.setInterval(originalLayout.getInterval());
                initialVersion.setUserSettings(originalLayout.getUserSettings());
                initialVersion.setCreatedAt(System.currentTimeMillis());
                initialVersion.setVersionType(VersionType.SNAPSHOT);
                initialVersion.setMemberId(memberId);

                initialVersion = studyLayoutVersionRepository.save(initialVersion);
                studyLayout.setCurrentVersionId(initialVersion.getId());
                studyLayoutRepository.save(studyLayout); // 更新版本 ID
            }
        }

        // 建立 StudyContentItem 排序
        if (!studyContentItemExists) {
            StudyContentItem studyContentItem = new StudyContentItem(
                    studyId, ContentType.LAYOUT, layoutId, nextSortOrder
            );
            studyContentItemRepository.save(studyContentItem);
            changesMade = true;
        }

        if (changesMade) {
            return Result.ok("Layout '" + layoutId + "' associated and ordered within study successfully.");
        } else {
            return Result.ok("Layout '" + layoutId + "' was already associated and ordered within study. No new changes made.");
        }
    }
}
