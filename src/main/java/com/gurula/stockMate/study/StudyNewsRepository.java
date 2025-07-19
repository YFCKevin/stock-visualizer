package com.gurula.stockMate.study;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudyNewsRepository extends MongoRepository<StudyNews, String> {
    List<StudyNews> findByStudyIdAndNewsIdIn(String studyId, List<String> newsIds);

    Optional<StudyNews> findByStudyIdAndNewsId(String studyId, String newsId);
}
