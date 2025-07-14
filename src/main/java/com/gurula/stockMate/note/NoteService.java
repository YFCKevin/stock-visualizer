package com.gurula.stockMate.note;

import com.gurula.stockMate.exception.Result;

import java.util.List;

public interface NoteService {
    Result<Note, String> save(NoteDTO noteDTO);

    Result<Note, String> edit(NoteDTO noteDTO);

    Result<List<Note>, String> findByLayoutId(String layoutId);

    Result<Note, String> findByIdAndMemberId(String noteId, String memberId);

    Result<String, String> delete(String noteId, String memberId);

    List<Note> search(String searchText);

    Result<Note, String> editTitle(NoteDTO noteDTO);
}
