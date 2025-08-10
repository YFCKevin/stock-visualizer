package com.gurula.stockMate.note;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.note.dto.CreatedNoteDTO;
import com.gurula.stockMate.note.dto.EditTitleNoteDTO;

import java.util.List;

public interface NoteService {
    Result<Note, String> save(CreatedNoteDTO noteDTO);

    Result<Note, String> edit(CreatedNoteDTO noteDTO);

    Result<List<Note>, String> findByLayoutId(String layoutId);

    Result<Note, String> findByIdAndMemberId(String noteId, String memberId);

    Result<String, String> delete(String noteId, String memberId);

    List<Note> search(String searchText);

    Result<Note, String> editTitle(EditTitleNoteDTO noteDTO);
}
