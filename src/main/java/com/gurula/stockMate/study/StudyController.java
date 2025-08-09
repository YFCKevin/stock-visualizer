package com.gurula.stockMate.study;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.member.Member;
import com.gurula.stockMate.member.MemberContext;
import com.gurula.stockMate.news.News;
import com.gurula.stockMate.news.NewsDTO;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.gurula.stockMate.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@RestController
@RequestMapping("/study")
@Tag(name = "Study API", description = "研究報告")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class StudyController {
    private final ImportService importService;
    private final StudyService studyService;

    public StudyController(ImportService importService, StudyService studyService) {
        this.importService = importService;
        this.studyService = studyService;
    }

    @Operation(summary = "新增一筆研究報告標題與描述")
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

    @Operation(summary = "修改研究報告標題與描述")
    @PatchMapping("/edit")
    public ResponseEntity<?> editStudy(@RequestBody StudyDTO studyDTO) {
        final Member member = MemberContext.getMember();
        studyDTO.setMemberId(member.getId());

        Result<String, String> result = studyService.edit(studyDTO);

        if (result.isOk()) {
            return ResponseEntity.ok(result.unwrap());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + result.unwrapErr());
        }
    }

    @Operation(summary = "封存研究報告")
    @PostMapping("/archive/{studyId}")
    public ResponseEntity<?> archiveStudy(@PathVariable String studyId) {
        final Member member = MemberContext.getMember();
        Result<String, String> result = studyService.archiveStudy(studyId, member.getId());

        if (result.isOk()) {
            return ResponseEntity.ok(result.unwrap());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + result.unwrapErr());
        }
    }

    @Operation(summary = "匯入研究報告內容")
    @PostMapping("/content-import")
    public ResponseEntity<?> importContent(@RequestBody ImportDTO importDTO) {
        Member member = MemberContext.getMember();
        Result<StudyContentDTO, String> result = switch (importDTO.getContentType()) {
            case NEWS -> importService.importNewsToStudy(importDTO, member.getId());
            case LAYOUT -> importService.importLayoutsToStudy(importDTO, member.getId());
            case NOTE -> importService.importNotesToStudy(importDTO, member.getId());
        };

        if (result.isOk()) {
            final StudyContentDTO dto = result.unwrap();
            if (StringUtils.isBlank(dto.getId())) {
                return ResponseEntity.ok("Content was already associated and ordered within study. No new changes made.");
            } else {
                return ResponseEntity.ok(result.unwrap());
            }
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

    @Operation(summary = "修改研究報告內容")
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

    @Operation(summary = "修改研究報告內容的標題")
    @PatchMapping("/edit-item-title")
    public ResponseEntity<?> editContentItemTitle(@RequestBody UpdateStudyContentDTO updateStudyContentDTO) {
        final Member member = MemberContext.getMember();
        updateStudyContentDTO.setMemberId(member.getId());

        final VersionType versionType = updateStudyContentDTO.getVersionType();
        if (versionType.equals(VersionType.SYNC)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Result.err("Modification of content items in synchronized versions is prohibited."));
        }

        Result<String, String> result = studyService.editContentItemTitle(updateStudyContentDTO);

        if (result.isOk()) {
            return ResponseEntity.ok(result.unwrap());
        } else {
            return ResponseEntity.badRequest().body(result.unwrapErr());
        }

    }

    @Operation(summary = "取得該會員所有的研究報告")
    @GetMapping
    public ResponseEntity<?> studies() {
        Member member = MemberContext.getMember();
        List<Study> studies = studyService.findStudies(member.getId());
        return ResponseEntity.ok(studies);
    }

    @Operation(summary = "取得一筆研究報告資訊，用於編輯研究報告之用")
    @GetMapping("/{studyId}")
    public ResponseEntity<?> getStudyById(@PathVariable String studyId) {
        final Member member = MemberContext.getMember();
        Result<Study, String> result = studyService.findById(studyId, member.getId());

        if (result.isOk()) {
            return ResponseEntity.ok(result.unwrap().toDto());
        } else {
            return ResponseEntity.badRequest().body(result.unwrapErr());
        }
    }

    @Operation(summary = "取得一筆研究報告資訊，用於顯示研究報告內容列表")
    @GetMapping("/{studyId}/contents")
    public ResponseEntity<?> getContentsForStudy(@PathVariable String studyId) {
        Member member = MemberContext.getMember();

        Result<List<StudyContentDTO>, String> result = studyService.getContentsByStudyId(studyId, member.getId());
        if (result.isOk()) {
            return ResponseEntity.ok(result.unwrap());
        } else {
            return ResponseEntity.badRequest().body(result.unwrapErr());
        }
    }

    @Operation(summary = "研究報告內容排序")
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

    @Operation(
            summary = "查詢內容",
            description = "根據內容類型與關鍵字查詢對應的內容清單",
            parameters = {
                    @Parameter(
                            name = "type",
                            description = "內容類型，例如：LAYOUT、NEWS、NOTE",
                            required = true,
                            in = ParameterIn.QUERY,
                            example = "LAYOUT"
                    ),
                    @Parameter(
                            name = "keyword",
                            description = "搜尋關鍵字，可選參數，用於模糊搜尋標題或描述",
                            required = false,
                            in = ParameterIn.QUERY,
                            example = "台積電"
                    )
            }
    )
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

    @Operation(summary = "刪除一筆研究報告內容")
    @DeleteMapping("/content-items")
    public ResponseEntity<?> removeContentItemFromStudy(@RequestBody RemoveContentItemDTO removeContentItemDTO) {
        final Member member = MemberContext.getMember();
        removeContentItemDTO.setMemberId(member.getId());
        Result<String, String> result = studyService.removeContentItemFromStudy(removeContentItemDTO);
        if (result.isOk()) {
            return ResponseEntity.ok(result.unwrap());
        } else {
            return ResponseEntity.badRequest().body(result.unwrapErr());
        }
    }

}
