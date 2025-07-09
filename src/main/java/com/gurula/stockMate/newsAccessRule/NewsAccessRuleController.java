package com.gurula.stockMate.newsAccessRule;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.member.Member;
import com.gurula.stockMate.member.MemberContext;
import com.gurula.stockMate.note.Note;
import com.gurula.stockMate.note.NoteDTO;
import com.gurula.stockMate.oauth.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/news/rule")
public class NewsAccessRuleController {
    private final NewsAccessRuleService newsAccessRuleService;

    public NewsAccessRuleController(NewsAccessRuleService newsAccessRuleService) {
        this.newsAccessRuleService = newsAccessRuleService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody NewsAccessRuleDTO ruleDTO) {
        final Member member = MemberContext.getMember();
        ruleDTO.setCreatedBy(member.getId());

        final VisibilityType visibility = ruleDTO.getVisibility();
        if ((visibility == VisibilityType.PUBLIC || visibility == VisibilityType.PRIVATE)
                && Role.ADMIN.equals(member.getRole())) {
            throw new IllegalArgumentException("只有管理者能新增 PUBLIC 或 PRIVATE 權限設定");
        }

        Result<NewsAccessRuleDTO, String> result = newsAccessRuleService.save(ruleDTO);

        if (result.isOk()) {
            return ResponseEntity.ok(result.unwrap());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", result.unwrapErr()));
        }
    }


    @PutMapping
    public ResponseEntity<?> edit(@RequestBody NewsAccessRuleDTO ruleDTO) {
        final Member member = MemberContext.getMember();
        ruleDTO.setCreatedBy(member.getId());

        final VisibilityType visibility = ruleDTO.getVisibility();
        if ((visibility == VisibilityType.PUBLIC || visibility == VisibilityType.PRIVATE)
                && Role.ADMIN.equals(member.getRole())) {
            throw new IllegalArgumentException("只有管理者能修改 PUBLIC 或 PRIVATE 權限設定");
        }

        if (visibility == VisibilityType.GROUP && (ruleDTO.getVisibleToGroupIds() == null || ruleDTO.getVisibleToGroupIds().isEmpty())) {
            throw new IllegalArgumentException("當 visibility 為 GROUP 時，visibleToGroupIds 不可為空");
        } else if (visibility == VisibilityType.RESTRICTED && (ruleDTO.getVisibleToMemberIds() == null || ruleDTO.getVisibleToMemberIds().isEmpty())) {
            throw new IllegalArgumentException("當 visibility 為 RESTRICTED 時，visibleToMemberIds 不可為空");
        }

        Result<NewsAccessRuleDTO, String> result = newsAccessRuleService.edit(ruleDTO);

        if (result.isOk()) {
            return ResponseEntity.ok(result.unwrap());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", result.unwrapErr()));
        }
    }


    @GetMapping
    public ResponseEntity<?> findAllByMemberId() {
        final Member member = MemberContext.getMember();

        Result<List<NewsAccessRuleDTO>, String> result = newsAccessRuleService.findAllByMemberId(member.getId());
        List<NewsAccessRuleDTO> ruleDTOList = result.unwrap();

        List<NewsAccessRuleDTO> allRules = new ArrayList<>();
        allRules.addAll(newsAccessRuleService.findByVisibilityIn(List.of(VisibilityType.PUBLIC, VisibilityType.PRIVATE)));
        allRules.addAll(ruleDTOList);

        Map<String, NewsAccessRuleDTO> deduplicatedMap = allRules.stream()
                .collect(Collectors.toMap(
                        NewsAccessRuleDTO::getId,
                        Function.identity(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        List<NewsAccessRuleDTO> deduplicatedRules = new ArrayList<>(deduplicatedMap.values());

        return ResponseEntity.ok(deduplicatedRules);
    }


    @GetMapping("/{ruleId}")
    public ResponseEntity<?> getOne(@PathVariable(name = "ruleId") String ruleId) {
        final Member member = MemberContext.getMember();

        Result<NewsAccessRuleDTO, String> result = newsAccessRuleService.findById(ruleId);

        if (result.isOk()) {
            return ResponseEntity.ok(result.unwrap());
        } else {
            String errorMessage = result.unwrapErr();

            if (errorMessage.equals("找不到對應的權限資料")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", errorMessage));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }
    }


    @DeleteMapping("/{ruleId}")
    public ResponseEntity<?> delete(@PathVariable(name = "ruleId") String ruleId) {
        final Member member = MemberContext.getMember();

        Result<String, String> result = newsAccessRuleService.deleteRule(member.getId(), ruleId);

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

            return switch (errorMessage) {
                case "找不到對應的 Rule 資料" -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
                case "無權限刪除此權限規則" -> ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
                case "無法刪除系統預設的 PUBLIC / PRIVATE 規則" -> ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
                default -> ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            };

        }
    }
}
