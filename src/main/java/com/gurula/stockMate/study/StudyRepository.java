package com.gurula.stockMate.study;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudyRepository extends MongoRepository<Study, String> {
    Optional<Study> findByIdAndMemberId(String id, String memberId);

    List<Study> findByMemberIdAndArchiveIsFalse(String memberId);
}