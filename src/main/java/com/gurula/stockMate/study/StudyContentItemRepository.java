package com.gurula.stockMate.study;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudyContentItemRepository extends MongoRepository<StudyContentItem, String> {
    Optional<StudyContentItem> findTopByStudyIdOrderBySortOrderDesc(String studyId);

    List<StudyContentItem> findByStudyIdAndContentTypeAndContentIdIn(String studyId, ContentType contentType, List<String> layoutIds);

    List<StudyContentItem> findByStudyIdOrderBySortOrderAsc(String studyId);

    Optional<StudyContentItem> findByStudyIdAndContentTypeAndContentId(String studyId, ContentType contentType, String newsId);
}
