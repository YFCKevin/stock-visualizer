package com.gurula.stockMate.note;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends MongoRepository<Note, String> {
    List<Note> findByLayoutId(String layoutId);
    List<Note> findByIdIn(List<String> ids);
    List<Note> findByIdInAndMemberId(List<String> noteIds, String memberId);

    Optional<Note> findByIdAndMemberId(String noteId, String memberId);

    List<Note> findByMemberId(String memberId);
}
