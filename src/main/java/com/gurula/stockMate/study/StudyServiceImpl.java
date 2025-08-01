package com.gurula.stockMate.study;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.layout.Layout;
import com.gurula.stockMate.layout.LayoutRepository;
import com.gurula.stockMate.layout.dto.LayoutDTO;
import com.gurula.stockMate.layout.dto.LayoutSummaryDTO;
import com.gurula.stockMate.news.News;
import com.gurula.stockMate.news.NewsDTO;
import com.gurula.stockMate.news.NewsRepository;
import com.gurula.stockMate.note.Note;
import com.gurula.stockMate.note.NoteDTO;
import com.gurula.stockMate.note.NoteRepository;
import com.gurula.stockMate.symbol.Symbol;
import com.gurula.stockMate.symbol.SymbolRepository;
import java.io.PrintStream;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConvertOperators.ToObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyServiceImpl implements StudyService {
    private final LayoutRepository layoutRepository;
    private final StudyLayoutRepository studyLayoutRepository;
    private final StudyRepository studyRepository;
    private final StudyContentItemRepository studyContentItemRepository;
    private final NoteRepository noteRepository;
    private final NewsRepository newsRepository;
    private final StudyNoteVersionRepository studyNoteVersionRepository;
    private final StudyNoteRepository studyNoteRepository;
    private final StudyLayoutVersionRepository studyLayoutVersionRepository;
    private final SymbolRepository symbolRepository;
    private final MongoTemplate mongoTemplate;
    private final StudyNewsRepository studyNewsRepository;

    public StudyServiceImpl(LayoutRepository layoutRepository, StudyLayoutRepository studyLayoutRepository, StudyRepository studyRepository, StudyContentItemRepository studyContentItemRepository, NoteRepository noteRepository, NewsRepository newsRepository, StudyNoteVersionRepository studyNoteVersionRepository, StudyNoteRepository studyNoteRepository, StudyLayoutVersionRepository studyLayoutVersionRepository, SymbolRepository symbolRepository, MongoTemplate mongoTemplate,
                            StudyNewsRepository studyNewsRepository) {
        this.layoutRepository = layoutRepository;
        this.studyLayoutRepository = studyLayoutRepository;
        this.studyRepository = studyRepository;
        this.studyContentItemRepository = studyContentItemRepository;
        this.noteRepository = noteRepository;
        this.newsRepository = newsRepository;
        this.studyNoteVersionRepository = studyNoteVersionRepository;
        this.studyNoteRepository = studyNoteRepository;
        this.studyLayoutVersionRepository = studyLayoutVersionRepository;
        this.symbolRepository = symbolRepository;
        this.mongoTemplate = mongoTemplate;
        this.studyNewsRepository = studyNewsRepository;
    }

    @Override
    public Result<List<StudyContentDTO>, String> getContentsByStudyId(String studyId, String memberId) {
        Optional<Study> studyOpt = studyRepository.findByIdAndMemberId(studyId, memberId);
        if (studyOpt.isEmpty()) {
            return Result.err("Study not found or not owned by member for ID: " + studyId);
        }

        List<StudyContentItem> contentItems = studyContentItemRepository.findByStudyIdOrderBySortOrderAsc(studyId);
        if (contentItems.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        // 查詢 StudyNote 和 StudyLayout
        Map<String, StudyLayout> studyLayoutMap = studyLayoutRepository.findByStudyId(studyId).stream()
                .collect(Collectors.toMap(
                        layout -> {
                            String versionId = layout.getCurrentVersionId();
                            return (versionId != null && !versionId.isBlank()) ? versionId : layout.getLayoutId();
                        },
                        Function.identity(),
                        (existing, replacement) -> replacement
                ));

        Map<String, StudyNote> studyNoteMap = studyNoteRepository.findByStudyId(studyId).stream()
                .collect(Collectors.toMap(
                        note -> {
                            String versionId = note.getCurrentVersionId();
                            return (versionId != null && !versionId.isBlank()) ? versionId : note.getNoteId();
                        },
                        Function.identity(),
                        (existing, replacement) -> replacement
                ));

        // 將 contentItem 分類
        Map<ContentType, List<String>> contentIdsByType = new EnumMap<>(ContentType.class);
        for (StudyContentItem item : contentItems) {
            contentIdsByType.computeIfAbsent(item.getContentType(), k -> new ArrayList<>()).add(item.getContentId());
        }

        // 處理 Layout
        Map<String, LayoutDTO> layoutDTOMap = new HashMap<>();
        if (contentIdsByType.containsKey(ContentType.LAYOUT)) {
            List<String> layoutIds = contentIdsByType.get(ContentType.LAYOUT);
            List<String> syncLayoutIds = new ArrayList<>();
            List<String> versionIds = new ArrayList<>();

            for (String layoutId : layoutIds) {
                StudyLayout sl = studyLayoutMap.get(layoutId);
                if (sl == null) return Result.err("Missing StudyLayout for layoutId: " + layoutId);
                if (sl.isSyncEnabled()) {
                    syncLayoutIds.add(layoutId);
                } else if (sl.getCurrentVersionId() != null) {
                    versionIds.add(sl.getCurrentVersionId());
                } else {
                    return Result.err("Unsynced StudyLayout with null versionId: " + sl.getId());
                }
            }

            final List<Layout> oriLayouts = layoutRepository.findByIdInAndMemberId(syncLayoutIds, memberId);
            Map<String, Layout> originalLayouts = oriLayouts.stream()
                    .collect(Collectors.toMap(Layout::getId, Function.identity()));

            final List<StudyLayoutVersion> snapshotLayouts = studyLayoutVersionRepository.findByIdIn(versionIds);
            Map<String, StudyLayoutVersion> layoutVersions = snapshotLayouts.stream()
                    .collect(Collectors.toMap(StudyLayoutVersion::getId, Function.identity()));

            Set<String> mergedSymbolIds = Stream.concat(
                    oriLayouts.stream().map(Layout::getSymbolId),
                    snapshotLayouts.stream().map(StudyLayoutVersion::getSymbolId)
            ).collect(Collectors.toSet());
            final Map<String, String> symbolIdToNameMap = symbolRepository.findByIdIn(mergedSymbolIds).stream()
                    .collect(Collectors.toMap(Symbol::getId, Symbol::getSymbol));


            for (String layoutId : layoutIds) {
                StudyLayout sl = studyLayoutMap.get(layoutId);
                LayoutDTO dto;
                if (sl.isSyncEnabled()) {
                    Layout origin = originalLayouts.get(layoutId);
                    if (origin == null) return Result.err("Original Layout not found: " + layoutId);
                    dto = origin.toDto();
                    dto.setSymbol(symbolIdToNameMap.get(origin.getSymbolId()));
                    dto.setVersionType(VersionType.SYNC);
                    dto.setSyncEnabled(true);
                } else {
                    StudyLayoutVersion ver = layoutVersions.get(sl.getCurrentVersionId());
                    if (ver == null) return Result.err("LayoutVersion not found: " + sl.getCurrentVersionId());
                    dto = ver.toDto();
                    dto.setSymbol(symbolIdToNameMap.get(ver.getSymbolId()));
                    dto.setSyncEnabled(false);
                }
                layoutDTOMap.put(layoutId, dto);
            }
        }

        // 處理 Note
        Map<String, NoteDTO> noteDTOMap = new HashMap<>();
        if (contentIdsByType.containsKey(ContentType.NOTE)) {
            List<String> noteIds = contentIdsByType.get(ContentType.NOTE);
            List<String> syncNoteIds = new ArrayList<>();
            List<String> versionIds = new ArrayList<>();

            for (String noteId : noteIds) {
                StudyNote sn = studyNoteMap.get(noteId);
                if (sn == null) return Result.err("Missing StudyNote for noteId: " + noteId);
                if (sn.isSyncEnabled()) {
                    syncNoteIds.add(noteId);
                } else if (sn.getCurrentVersionId() != null) {
                    versionIds.add(sn.getCurrentVersionId());
                } else {
                    return Result.err("Unsynced StudyNote with null versionId: " + sn.getId());
                }
            }

            Map<String, Note> originalNotes = noteRepository.findByIdInAndMemberId(syncNoteIds, memberId).stream()
                    .collect(Collectors.toMap(Note::getId, Function.identity()));

            Map<String, StudyNoteVersion> noteVersions = studyNoteVersionRepository.findByIdIn(versionIds).stream()
                    .collect(Collectors.toMap(StudyNoteVersion::getId, Function.identity()));

            for (String noteId : noteIds) {
                StudyNote sn = studyNoteMap.get(noteId);
                NoteDTO dto;
                if (sn.isSyncEnabled()) {
                    Note origin = originalNotes.get(noteId);
                    if (origin == null) return Result.err("Original Note not found: " + noteId);
                    dto = origin.toDto();
                    dto.setVersionType(VersionType.SYNC);
                    dto.setSyncEnabled(true);
                } else {
                    StudyNoteVersion ver = noteVersions.get(sn.getCurrentVersionId());
                    if (ver == null) return Result.err("NoteVersion not found: " + sn.getCurrentVersionId());
                    dto = ver.toDto();
                    dto.setSyncEnabled(false);
                }
                noteDTOMap.put(noteId, dto);
            }
        }

        // 處理 News
        Map<String, NewsDTO> newsDTOMap = new HashMap<>();
        if (contentIdsByType.containsKey(ContentType.NEWS)) {
            newsDTOMap = newsRepository.findByIdInAndMemberId(contentIdsByType.get(ContentType.NEWS), memberId).stream()
                    .map(news -> {
                        final NewsDTO dto = news.toDto();
                        dto.setSyncEnabled(true);
                        return dto;
                    })
                    .collect(Collectors.toMap(NewsDTO::getId, Function.identity()));
        }

        // 組合為結果
        List<StudyContentDTO> resultList = new ArrayList<>();
        for (StudyContentItem item : contentItems) {
            StudyContentDTO dto = new StudyContentDTO();
            dto.setId(item.getContentId());
            dto.setType(item.getContentType());
            dto.setSortOrder(item.getSortOrder());

            switch (item.getContentType()) {
                case LAYOUT -> {
                    LayoutDTO layoutDTO = layoutDTOMap.get(item.getContentId());
                    dto.setData(layoutDTO);
                    dto.setTitle(layoutDTO.getName());
                    dto.setSyncEnabled(layoutDTO.isSyncEnabled());
                }
                case NOTE -> {
                    NoteDTO noteDTO = noteDTOMap.get(item.getContentId());
                    dto.setData(noteDTO);
                    dto.setTitle(noteDTO.getTitle());
                    dto.setSyncEnabled(noteDTO.isSyncEnabled());
                }
                case NEWS -> {
                    NewsDTO newsDTO = newsDTOMap.get(item.getContentId());
                    dto.setData(newsDTO);
                    dto.setTitle(newsDTO.getTitle());
                    dto.setSyncEnabled(newsDTO.isSyncEnabled());
                }
            }

            resultList.add(dto);
        }

        return Result.ok(resultList);
    }

    @Override
    public List<Study> findStudies(String memberId) {
        return this.studyRepository.findByMemberIdAndArchiveIsFalse(memberId);
    }

    @Override
    public Result<Study, String> saveTitleAndDesc(StudyDTO studyDTO, String memberId) {
        try {
            Study study = new Study();
            study.setTitle(studyDTO.getTitle());
            study.setDesc(studyDTO.getDesc());
            study.setMemberId(memberId);
            study.setCreatedAt(System.currentTimeMillis());
            Study saved = this.studyRepository.save(study);
            return Result.ok(saved);
        } catch (Exception e) {
            return Result.err("儲存失敗：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String, String> updateStudyContentItemsOrder(StudyContentReorderDTO dto, String memberId) {
        String studyId = dto.getStudyId();
        List<StudyContentOrderItemDTO> reorderedItems = dto.getReorderedItems();

        Optional<Study> studyOpt = studyRepository.findByIdAndMemberId(studyId, memberId);
        if (studyOpt.isEmpty()) {
            return Result.err("Study not found or not owned by member for ID: " + studyId);
        }

        // 取出目前該 study 的所有內容項目（按 sortOrder 排序）
        List<StudyContentItem> currentItems = studyContentItemRepository.findByStudyIdOrderBySortOrderAsc(studyId);
        Map<String, StudyContentItem> currentItemMap = currentItems.stream()
                .collect(Collectors.toMap(StudyContentItem::getContentId, Function.identity()));

        List<StudyContentItem> itemsToUpdate = new ArrayList<>();
        boolean changesMade = false;

        for (int i = 0; i < reorderedItems.size(); i++) {
            StudyContentOrderItemDTO orderItem = reorderedItems.get(i);
            StudyContentItem currentItem = currentItemMap.get(orderItem.getId());

            if (currentItem == null) {
                System.err.printf("Warning: Reordered item not found in study - Content ID: %s, Type: %s%n",
                        orderItem.getContentId(), orderItem.getContentType());
                continue;
            }

            if (currentItem.getSortOrder() != i) {
                currentItem.setSortOrder(i);
                itemsToUpdate.add(currentItem);
                changesMade = true;
            }
        }

        if (changesMade && !itemsToUpdate.isEmpty()) {
            studyContentItemRepository.saveAll(itemsToUpdate);
            return Result.ok("Study content items order updated successfully for Study ID: " + studyId);
        } else if (!changesMade) {
            return Result.ok("Study content items order is already at the desired state. No changes made.");
        } else {
            return Result.err("No valid content items found to update or unexpected error occurred.");
        }
    }

    @Override
    @Transactional
    public Result<String, String> updateStudyNoteSyncState(ImportDTO importDTO, String memberId) {
        String studyId = importDTO.getSelfId();
        String noteId = importDTO.getContentId();
        boolean newSyncState = importDTO.isSyncEnabled();

        // 驗證 Study 是否存在且為該 member 擁有
        Optional<Study> studyOpt = studyRepository.findByIdAndMemberId(studyId, memberId);
        if (studyOpt.isEmpty()) {
            return Result.err("Study not found or not owned by member for ID: " + studyId);
        }

        // 驗證 Note 是否存在且為該 member 擁有
        Optional<Note> noteOpt = noteRepository.findByIdAndMemberId(noteId, memberId);
        if (noteOpt.isEmpty()) {
            return Result.err("Invalid or unauthorized Note ID found: " + noteId);
        }
        Note originalNote = noteOpt.get();

        // 找出 StudyNote 關聯
        Optional<StudyNote> studyNoteOpt = studyNoteRepository.findByStudyIdAndNoteId(studyId, noteId);
        if (studyNoteOpt.isEmpty()) {
            return Result.err("StudyNote association not found for Study ID: " + studyId + " and Note ID: " + noteId);
        }

        StudyNote studyNote = studyNoteOpt.get();
        boolean currentSyncState = studyNote.isSyncEnabled();

        // 若狀態未變則不需處理
        if (currentSyncState == newSyncState) {
            return Result.ok("StudyNote sync state already at desired value. No change made.");
        }

        // 狀態改變：處理切換
        studyNote.setSyncEnabled(newSyncState);

        if (!newSyncState) {
            // 關閉同步時建立 snapshot
            StudyNoteVersion snapshot = new StudyNoteVersion();
            snapshot.setStudyNoteId(studyNote.getId());
            snapshot.setTitle(originalNote.getTitle());
            snapshot.setContent(originalNote.getContent());
            snapshot.setCreatedAt(System.currentTimeMillis());
            snapshot.setVersionType(VersionType.SNAPSHOT);
            snapshot.setMemberId(memberId);

            snapshot = studyNoteVersionRepository.save(snapshot);
            studyNote.setCurrentVersionId(snapshot.getId());
        } else {
            // 開啟同步時移除版本綁定
            studyNote.setCurrentVersionId(null);
        }

        studyNoteRepository.save(studyNote);
        return Result.ok("StudyNote sync state updated successfully to " +
                (newSyncState ? "enabled" : "disabled") + " for Note '" + noteId + "'.");
    }

    @Override
    @Transactional
    public Result<String, String> updateStudyLayoutSyncState(ImportDTO importDTO, String memberId) {
        String studyId = importDTO.getSelfId();
        String layoutId = importDTO.getContentId();
        boolean newSyncState = importDTO.isSyncEnabled();

        // 驗證 Study 是否存在且為該 member 擁有
        Optional<Study> studyOpt = studyRepository.findByIdAndMemberId(studyId, memberId);
        if (studyOpt.isEmpty()) {
            return Result.err("Study not found or not owned by member for ID: " + studyId);
        }

        // 驗證 Layout 是否存在且為該 member 擁有
        Optional<Layout> layoutOpt = layoutRepository.findByIdAndMemberId(layoutId, memberId);
        if (layoutOpt.isEmpty()) {
            return Result.err("Invalid or unauthorized Layout ID found: " + layoutId);
        }
        Layout originalLayout = layoutOpt.get();

        // 查找關聯的 StudyLayout
        Optional<StudyLayout> studyLayoutOpt = studyLayoutRepository.findByStudyIdAndLayoutId(studyId, layoutId);
        if (studyLayoutOpt.isEmpty()) {
            return Result.err("StudyLayout association not found for Study ID: " + studyId + " and Layout ID: " + layoutId);
        }

        StudyLayout studyLayout = studyLayoutOpt.get();
        boolean currentSyncState = studyLayout.isSyncEnabled();

        if (currentSyncState == newSyncState) {
            return Result.ok("StudyLayout sync state already at desired value. No change made.");
        }

        // 狀態變更處理
        studyLayout.setSyncEnabled(newSyncState);

        if (!newSyncState) {
            // 關閉同步時產生一份 snapshot 版本
            StudyLayoutVersion snapshot = new StudyLayoutVersion();
            snapshot.setStudyLayoutId(studyLayout.getId());
            snapshot.setName(originalLayout.getName());
            snapshot.setDesc(originalLayout.getDesc());
            snapshot.setInterval(originalLayout.getInterval());
            snapshot.setUserSettings(originalLayout.getUserSettings());
            snapshot.setCreatedAt(System.currentTimeMillis());
            snapshot.setVersionType(VersionType.SNAPSHOT);
            snapshot.setMemberId(memberId);

            snapshot = studyLayoutVersionRepository.save(snapshot);
            studyLayout.setCurrentVersionId(snapshot.getId());
        } else {
            // 開啟同步時移除版本綁定
            studyLayout.setCurrentVersionId(null);
        }

        studyLayoutRepository.save(studyLayout);
        return Result.ok("StudyLayout sync state updated successfully to " +
                (newSyncState ? "enabled" : "disabled") + " for Layout '" + layoutId + "'.");
    }

    @Override
    public Result<String, String> updateStudyNews(ImportDTO importDTO, String memberId) {
        return null;
    }

    @Override
    public List<LayoutSummaryDTO> searchLayoutContent(String memberId, String keyword) {
        List<AggregationOperation> pipeline = new ArrayList<>();

        // 將 symbolId 欄位轉為 ObjectId，方便做 lookup
        pipeline.add(Aggregation.addFields()
                .addField("symbolId").withValue(ToObjectId.toObjectId("$symbolId"))
                .build());

        // 以 symbolId 去 lookup symbol collection
        pipeline.add(Aggregation.lookup("symbol", "symbolId", "_id", "symbolDoc"));

        // 將 lookup 結果解開（unwind）
        pipeline.add(Aggregation.unwind("symbolDoc", true));

        // 準備查詢條件
        List<Criteria> criteriaList = new ArrayList<>();
        if (StringUtils.isNotBlank(memberId)) {
            criteriaList.add(Criteria.where("memberId").is(memberId));
        }

        if (StringUtils.isNotBlank(keyword)) {
            // 忽略大小寫的模糊查詢條件（用正則）
            Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
            Criteria keywordCriteria = new Criteria().orOperator(
                    Criteria.where("name").regex(pattern),
                    Criteria.where("symbolDoc.symbol").regex(pattern),
                    Criteria.where("symbolDoc.name").regex(pattern)
            );
            criteriaList.add(keywordCriteria);
        }

        // 加入 match 條件，若條件非空
        if (!criteriaList.isEmpty()) {
            pipeline.add(Aggregation.match(new Criteria().andOperator(criteriaList.toArray(new Criteria[0]))));
        }

        // 建立 Aggregation 並執行
        Aggregation aggregation = Aggregation.newAggregation(pipeline);
        AggregationResults<Layout> results = mongoTemplate.aggregate(aggregation, "layout", Layout.class);
        List<Layout> layouts = results.getMappedResults();

        // 取出所有相關 symbolId 並一次查 symbol repository
        Set<String> symbolSet = layouts.stream()
                .map(Layout::getSymbolId)
                .collect(Collectors.toSet());

        Map<String, Symbol> symbolMap = symbolRepository.findByIdIn(symbolSet).stream()
                .collect(Collectors.toMap(Symbol::getId, Function.identity()));

        // 將結果封裝為 LayoutSummaryDTO
        return layouts.stream().map(layout -> {
            LayoutSummaryDTO dto = LayoutSummaryDTO.construct(layout);
            Symbol symbol = symbolMap.get(layout.getSymbolId());
            if (symbol != null) {
                dto.setSymbol(symbol.getSymbol());
                dto.setSymbolName(symbol.getName());
            }
            return dto;
        }).collect(Collectors.toList());
    }


    @Override
    public List<NoteDTO> searchNoteContent(String memberId, String keyword) {
        Criteria criteria = Criteria.where("memberId").is(memberId);

        if (StringUtils.isNotBlank(keyword)) {
            Criteria keywordCriteria = new Criteria().orOperator(
                    Criteria.where("title").regex(keyword, "i"),
                    Criteria.where("content").regex(keyword, "i")
            );
            criteria = new Criteria().andOperator(criteria, keywordCriteria);
        }

        Query query = new Query(criteria);
        List<Note> notes = mongoTemplate.find(query, Note.class);

        List<String> distinctLayoutIds = notes.stream()
                .map(Note::getLayoutId)
                .distinct()
                .toList();

        Map<String, String> layoutIdToNameMap = layoutRepository.findByIdInAndMemberId(distinctLayoutIds, memberId)
                .stream()
                .collect(Collectors.toMap(Layout::getId, Layout::getName));

        List<NoteDTO> noteDTOList = notes.stream()
                .map(note -> {
                    NoteDTO dto = note.toDto();
                    dto.setLayoutName(layoutIdToNameMap.get(note.getLayoutId()));
                    return dto;
                })
                .toList();

        return noteDTOList;
    }


    @Override
    public List<News> searchNewsContent(String memberId, String keyword) {
        Criteria criteria = Criteria.where("memberId").is(memberId);

        if (keyword != null && !keyword.trim().isEmpty()) {
            criteria = new Criteria().andOperator(
                    criteria,
                    Criteria.where("title").regex(keyword, "i")
            );
        }

        Query query = new Query(criteria);
        return mongoTemplate.find(query, News.class);
    }

    @Override
    public Result<String, String> editContentItemTitle(UpdateStudyContentDTO updateStudyContentDTO) {

        try {
            final String contentId = updateStudyContentDTO.getContentId();
            final String title = updateStudyContentDTO.getTitle();
            final ContentType contentType = updateStudyContentDTO.getContentType();
            final String memberId = updateStudyContentDTO.getMemberId();

            switch (contentType) {
                case LAYOUT -> {
                    Optional<StudyLayoutVersion> layoutOpt = studyLayoutVersionRepository.findByIdAndMemberId(contentId, memberId);
                    if (layoutOpt.isPresent()) {
                        StudyLayoutVersion layout = layoutOpt.get();
                        layout.setName(title);
                        studyLayoutVersionRepository.save(layout);
                    } else {
                        return Result.err("找不到對應的版面資料");
                    }
                }
                case NOTE -> {
                    Optional<StudyNoteVersion> noteOpt = studyNoteVersionRepository.findByIdAndMemberId(contentId, memberId);
                    if (noteOpt.isPresent()) {
                        StudyNoteVersion note = noteOpt.get();
                        note.setTitle(title);
                        studyNoteVersionRepository.save(note);
                    } else {
                        return Result.err("找不到對應的筆記資料");
                    }
                }
            }
            return Result.ok(title);
        } catch (Exception e) {
            return Result.err("儲存失敗：" + e.getMessage());
        }

    }

    @Override
    public Result<String, String> removeContentItemFromStudy(RemoveContentItemDTO removeContentItemDTO) {
        final ContentType contentType = removeContentItemDTO.getContentType();
        final String memberId = removeContentItemDTO.getMemberId();
        final String contentId = removeContentItemDTO.getContentId();
        final boolean syncEnabled = removeContentItemDTO.isSyncEnabled();

        Optional<StudyContentItem> contentItemOptional = studyContentItemRepository.findByContentId(contentId);

        if (contentItemOptional.isEmpty()) {
            return Result.err("Content item association not found for the given contentId.");
        }

        final StudyContentItem studyContentItem = contentItemOptional.get();
        final String studyId = studyContentItem.getStudyId();

        // 2. 驗證 Study 是否存在且屬於 MemberId
        Optional<Study> studyOptional = studyRepository.findByIdAndMemberId(studyId, memberId);
        if (studyOptional.isEmpty()) {
            return Result.err("Study not found or unauthorized for this member.");
        }

        try {
            switch (contentType) {
                case LAYOUT -> {
                    Optional<StudyLayout> studyLayoutOptional;
                    if (syncEnabled) {
                        studyLayoutOptional = studyLayoutRepository.findByStudyIdAndLayoutId(studyId, contentId);
                    } else {
                        studyLayoutOptional = studyLayoutRepository.findByStudyIdAndCurrentVersionId(studyId, contentId);
                    }
                    if (studyLayoutOptional.isEmpty()) {
                        return Result.err("Layout not found in the specified study or version mismatch.");
                    }
                    studyLayoutRepository.delete(studyLayoutOptional.get());
                }
                case NOTE -> {
                    Optional<StudyNote> studyNoteOptional;
                    if (syncEnabled) {
                        studyNoteOptional = studyNoteRepository.findByStudyIdAndNoteId(studyId, contentId);
                    } else {
                        studyNoteOptional = studyNoteRepository.findByStudyIdAndCurrentVersionId(studyId, contentId);
                    }
                    if (studyNoteOptional.isEmpty()) {
                        return Result.err("Note not found in the specified study or version mismatch.");
                    }
                    studyNoteRepository.delete(studyNoteOptional.get());
                }
                case NEWS -> {
                    Optional<StudyNews> studyNewsOptional = studyNewsRepository.findByStudyIdAndNewsId(studyId, contentId);
                    if (studyNewsOptional.isEmpty()) {
                        return Result.err("News item not found in the specified study.");
                    }
                    studyNewsRepository.delete(studyNewsOptional.get());
                }
                default -> {
                    return Result.err("Unsupported content type: " + contentType);
                }
            }

            studyContentItemRepository.delete(studyContentItem);

            return Result.ok("Content item successfully removed from study.");

        } catch (Exception e) {
            return Result.err("An error occurred during content item removal: " + e.getMessage());
        }
    }

    @Override
    public Result<Study, String> findById(String studyId, String memberId) {
        final Optional<Study> studyOptional = studyRepository.findById(studyId);
        if (studyOptional.isEmpty()) {
            return Result.err("Study not found or not owned by member for ID: " + studyId);
        } else {
            return Result.ok(studyOptional.get());
        }
    }

    @Override
    public Result<String, String> edit(StudyDTO studyDTO) {
        try {
            final String studyId = studyDTO.getId();
            final Optional<Study> studyOptional = studyRepository.findById(studyId);
            if (studyOptional.isEmpty()) {
                return Result.err("Study not found or not owned by member for ID: " + studyId);
            } else {
                final Study study = studyOptional.get();
                if (StringUtils.isNotBlank(studyDTO.getDesc()))
                    study.setDesc(studyDTO.getDesc());
                if (StringUtils.isNotBlank(studyDTO.getTitle()))
                    study.setTitle(studyDTO.getTitle());
                studyRepository.save(study);
                return Result.ok("編輯成功");
            }
        } catch (Exception e) {
            return Result.err("儲存失敗：" + e.getMessage());
        }
    }

    @Override
    public Result<String, String> archiveStudy(String studyId, String memberId) {
        try {
            final Optional<Study> studyOptional = studyRepository.findByIdAndMemberId(studyId, memberId);
            if (studyOptional.isEmpty()) {
                return Result.err("Study not found or not owned by member for ID: " + studyId);
            }

            final Study study = studyOptional.get();
            study.setArchive(!study.isArchive());
            studyRepository.save(study);
            return Result.ok("封存成功");

        } catch (Exception e) {
            return Result.err("封存失敗：" + e.getMessage());
        }
    }
}
