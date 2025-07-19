package com.gurula.stockMate.study;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudyLayoutVersionRepository extends MongoRepository<StudyLayoutVersion, String> {
    List<StudyLayoutVersion> findByIdIn(List<String> layoutVersionIds);
}
