package com.gurula.stockMate.study;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudyLayoutVersionRepository extends MongoRepository<StudyLayoutVersion, String> {
    List<StudyLayoutVersion> findByIdIn(List<String> layoutVersionIds);
    Optional<StudyLayoutVersion> findByIdAndMemberId(String contentId, String memberId);
}
