package com.gurula.stockMate.study;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.member.Member;
import com.gurula.stockMate.member.MemberContext;
import com.gurula.stockMate.news.News;
import com.gurula.stockMate.news.NewsDTO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/study"})
public class StudyController {
    private final ImportService importService;
    private final StudyService studyService;

    public StudyController(ImportService importService, StudyService studyService) {
        this.importService = importService;
        this.studyService = studyService;
    }

    @PostMapping
    public ResponseEntity<?> writeStudy(@RequestBody StudyDTO studyDTO) {
        Member member = MemberContext.getMember();
        Result<Study, String> result = studyService.saveTitleAndDesc(studyDTO, member.getId());
        if (result.isOk()) {
            Study study = result.unwrap();
            StudyDTO dto = study.toDto();
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", result.unwrapErr()));
        }
    }

    @PostMapping("/content-import")
    public ResponseEntity<?> importContent(@RequestBody ImportDTO importDTO) {
        Member member = MemberContext.getMember();
        Result<String, String> result = switch (importDTO.getContentType()) {
            case NEWS -> importService.importNewsToStudy(importDTO, member.getId());
            case LAYOUT -> importService.importLayoutsToStudy(importDTO, member.getId());
            case NOTE -> importService.importNotesToStudy(importDTO, member.getId());
        };

        if (result.isOk()) {
            return ResponseEntity.ok(result.unwrap());
        } else {
            String errorMessage = result.unwrapErr();
            if (errorMessage.contains("not found or not owned")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            } else {
                if (errorMessage.contains("not found or not owned")) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
                } else if (errorMessage.contains("No News IDs provided") ||
                        errorMessage.contains("Invalid or unauthorized")) {
                    return ResponseEntity.badRequest().body(errorMessage);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("An error occurred: " + errorMessage);
                }
            }
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<?> edit(@RequestBody ImportDTO importDTO) {
        Member member = MemberContext.getMember();
        Result<String, String> result = switch (importDTO.getContentType()) {
            case NEWS -> studyService.updateStudyNews(importDTO, member.getId());
            case LAYOUT -> studyService.updateStudyLayoutSyncState(importDTO, member.getId());
            case NOTE -> studyService.updateStudyNoteSyncState(importDTO, member.getId());
        };

        if (result.isOk()) {
            return ResponseEntity.ok(result.unwrap());
        } else {
            String errorMessage = result.unwrapErr();
            if (errorMessage.contains("not found or not owned")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
            } else if (errorMessage.contains("No News IDs provided") ||
                    errorMessage.contains("Invalid or unauthorized")) {
                return ResponseEntity.badRequest().body(errorMessage);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("An error occurred: " + errorMessage);
            }
        }
    }

    @GetMapping
    public ResponseEntity<?> studies() {
        Member member = MemberContext.getMember();
        List<Study> studies = studyService.findStudies(member.getId());
        return ResponseEntity.ok(studies);
    }

    @GetMapping("/{studyId}/contents")
    public ResponseEntity<?> getContentsForStudy(@PathVariable String studyId) {
        Member member = MemberContext.getMember();

        try {
            Result<List<StudyContentDTO>, String> result = studyService.getContentsByStudyId(studyId, member.getId());
            return ResponseEntity.ok(result.unwrap());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @PutMapping("/reorder")
    public ResponseEntity<?> reorderItems(@RequestBody StudyContentReorderDTO dto) {
        Member member = MemberContext.getMember();

        try {
            Result<String, String> result = studyService.updateStudyContentItemsOrder(dto, member.getId());
            return ResponseEntity.ok(result.unwrap());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/content")
    public ResponseEntity<?> searchContent(@RequestParam ContentType type,
                                           @RequestParam(required = false) String keyword) {
        Member member = MemberContext.getMember();

        List<?> result = switch (type) {
            case NEWS -> studyService.searchNewsContent(member.getId(), keyword)
                    .stream()
                    .map(News::toDto)
                    .toList();

            case LAYOUT -> studyService.searchLayoutContent(member.getId(), keyword);

            case NOTE -> studyService.searchNoteContent(member.getId(), keyword);
        };

        return ResponseEntity.ok(result);
    }
}
