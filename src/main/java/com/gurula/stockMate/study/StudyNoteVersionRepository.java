package com.gurula.stockMate.study;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudyNoteVersionRepository extends MongoRepository<StudyNoteVersion, String> {
    List<StudyNoteVersion> findByIdIn(List<String> versionIds);

    Optional<StudyNoteVersion> findByIdAndMemberId(String contentId, String memberId);
}
