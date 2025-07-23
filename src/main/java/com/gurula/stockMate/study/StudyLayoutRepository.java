package com.gurula.stockMate.study;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudyLayoutRepository extends MongoRepository<StudyLayout, String> {
    List<StudyLayout> findByStudyId(String studyId);

    Optional<StudyLayout> findByStudyIdAndLayoutId(String studyId, String layoutId);

    Optional<StudyLayout> findByStudyIdAndCurrentVersionId(String studyId, String contentId);
}
