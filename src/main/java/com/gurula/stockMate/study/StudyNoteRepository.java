package com.gurula.stockMate.study;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudyNoteRepository extends MongoRepository<StudyNote, String> {
    Optional<StudyNote> findByStudyIdAndNoteId(String studyId, String noteId);

    List<StudyNote> findByStudyId(String studyId);
}
