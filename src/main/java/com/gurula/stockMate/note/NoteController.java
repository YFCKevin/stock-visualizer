package com.gurula.stockMate.note;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.member.Member;
import com.gurula.stockMate.member.MemberContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gurula.stockMate.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@RestController
@RequestMapping("/note")
@Tag(name = "Note API", description = "筆記")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @Operation(summary = "儲存筆記")
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody NoteDTO noteDTO) {
        final Member member = MemberContext.getMember();
        noteDTO.setMemberId(member.getId());

        try {
            Result<Note, String> result;
            if (StringUtils.isBlank(noteDTO.getId())) { // 新增
                result = noteService.save(noteDTO);
            } else {
                result = noteService.edit(noteDTO);
            }

            if (result.isOk()) {
                Note note = result.unwrap();
                final NoteDTO dto = note.toDto();
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", result.unwrapErr()));
            }

        } catch (Exception e) {
            String message = e.getMessage();

            if (message.contains("找不到對應的 Note 資料")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", message));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", message));
        }
    }

    /**
     * 查詢每個版面的所有筆記
     * @param layoutId
     * @return
     */
    @Operation(summary = "查詢每個版面的所有筆記")
    @GetMapping("/layout/{layoutId}")
    public ResponseEntity<?> findByLayoutId(@PathVariable(name = "layoutId") String layoutId) {
        final Member member = MemberContext.getMember();

        try {
            Result<List<Note>, String> result = noteService.findByLayoutId(layoutId);
            if (result.isOk()) {
                final List<Note> notes = result.unwrap();
                final List<NoteDTO> noteDTOList = notes.stream().map(Note::toDto).toList();
                return ResponseEntity.ok(noteDTOList);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", result.unwrapErr()));
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            if (errorMessage.equals("找不到對應的 Note 資料")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", errorMessage));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }
    }

    @Operation(summary = "查詢一則筆記內容")
    @GetMapping("/{noteId}")
    public ResponseEntity<?> getNoteById(@PathVariable(name = "noteId") String noteId) {
        final Member member = MemberContext.getMember();

        try {
            Result<Note, String> result = noteService.findByIdAndMemberId(noteId, member.getId());
            if (result.isOk()) {
                final Note note = result.unwrap();
                return ResponseEntity.ok(note.toDto());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", result.unwrapErr()));
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            if (errorMessage.equals("找不到對應的 Note 資料")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", errorMessage));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }
    }

    @Operation(summary = "刪除筆記")
    @DeleteMapping("/{noteId}")
    public ResponseEntity<?> delete(@PathVariable(name = "noteId") String noteId) {
        final Member member = MemberContext.getMember();
        Map<String, Object> response = new HashMap<>();

        try {
            Result<String, String> result = noteService.delete(noteId, member.getId());
            if (result.isOk()) {
                response.put("message", result.unwrap());
                return ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            } else {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
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

    @Operation(summary = "修改筆記標題")
    @PatchMapping
    public ResponseEntity<?> editTitle(@RequestBody NoteDTO noteDTO) {
        final Member member = MemberContext.getMember();
        noteDTO.setMemberId(member.getId());

        try {
            Result<Note, String> result = noteService.editTitle(noteDTO);

            if (result.isOk()) {
                Note note = result.unwrap();
                final NoteDTO dto = note.toDto();
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", result.unwrapErr()));
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            if (errorMessage.equals("找不到對應的 Note 資料")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", errorMessage));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }
    }

    @Operation(
            summary = "搜尋筆記的標題和內容",
            description = "根據輸入的關鍵字進行模糊搜尋，回傳符合的結果清單",
            parameters = {
                    @Parameter(
                            name = "searchText",
                            description = "關鍵字，可用於搜尋標題、內容描述等欄位",
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "台灣加權"
                    )
            }
    )
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String searchText
    ) {
        final Member member = MemberContext.getMember();
        List<Note> notes = noteService.search(searchText);
        return ResponseEntity.ok(notes);
    }
}
