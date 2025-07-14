package com.gurula.stockMate.note;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.member.Member;
import com.gurula.stockMate.member.MemberContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/note")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody NoteDTO noteDTO) {
        final Member member = MemberContext.getMember();
        noteDTO.setMemberId(member.getId());
        Result<Note, String> result;
        if (StringUtils.isBlank(noteDTO.getId())) { // 新增
            noteDTO.setTitle("無標題");
            result = noteService.save(noteDTO);
        } else {
            result = noteService.edit(noteDTO);
        }
        if (result.isOk()) {
            Note note = result.unwrap();
            final NoteDTO dto = note.toDto();
            return ResponseEntity.ok(dto);
        } else {
            String errorMessage = result.unwrapErr();

            if (errorMessage.equals("找不到對應的 Note 資料")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", errorMessage));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }
    }

    /**
     * 查詢每個版面的所有筆記
     * @param layoutId
     * @return
     */
    @GetMapping("/layout/{layoutId}")
    public ResponseEntity<?> findByLayoutId(@PathVariable(name = "layoutId") String layoutId) {
        final Member member = MemberContext.getMember();
        Result<List<Note>, String> result = noteService.findByLayoutId(layoutId);
        if (result.isOk()) {
            final List<Note> notes = result.unwrap();
            final List<NoteDTO> noteDTOList = notes.stream().map(Note::toDto).toList();
            return ResponseEntity.ok(noteDTOList);
        } else {
            String errorMessage = result.unwrapErr();

            if (errorMessage.equals("找不到對應的 Note 資料")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", errorMessage));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }
    }


    @GetMapping("/{noteId}")
    public ResponseEntity<?> getNoteById(@PathVariable(name = "noteId") String noteId) {
        final Member member = MemberContext.getMember();
        Result<Note, String> result = noteService.findByIdAndMemberId(noteId, member.getId());

        if (result.isOk()) {
            final Note note = result.unwrap();
            return ResponseEntity.ok(note.toDto());
        } else {
            String errorMessage = result.unwrapErr();

            if (errorMessage.equals("找不到對應的 Note 資料")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", errorMessage));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }
    }


    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> delete(@PathVariable(name = "noteId") String noteId) {
        final Member member = MemberContext.getMember();
        Result<String, String> result = noteService.delete(noteId, member.getId());

        Map<String, Object> response = new HashMap<>();

        if (result.isOk()) {
            response.put("message", result.unwrap());
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } else {
            String errorMessage = result.unwrapErr();
            response.put("error", errorMessage);

            if (errorMessage.equals("找不到對應的 Note 資料")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }


    @PatchMapping
    public ResponseEntity<?> editTitle(@RequestBody NoteDTO noteDTO) {
        final Member member = MemberContext.getMember();
        noteDTO.setMemberId(member.getId());
        Result<Note, String> result = noteService.editTitle(noteDTO);

        if (result.isOk()) {
            Note note = result.unwrap();
            final NoteDTO dto = note.toDto();
            return ResponseEntity.ok(dto);
        } else {
            String errorMessage = result.unwrapErr();

            if (errorMessage.equals("找不到對應的 Note 資料")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", errorMessage));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }
    }


    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String searchText
    ) {
        final Member member = MemberContext.getMember();
        List<Note> notes = noteService.search(searchText);
        return ResponseEntity.ok(notes);
    }
}
