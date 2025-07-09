package com.gurula.stockMate.newsAccessRule;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.member.Member;
import com.gurula.stockMate.member.MemberRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NewsAccessRuleServiceImpl implements NewsAccessRuleService {
    private final NewsAccessRuleRepository newsAccessRuleRepository;
    private final MemberRepository memberRepository;

    public NewsAccessRuleServiceImpl(NewsAccessRuleRepository newsAccessRuleRepository, MemberRepository memberRepository) {
        this.newsAccessRuleRepository = newsAccessRuleRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public Result<NewsAccessRuleDTO, String> save(NewsAccessRuleDTO ruleDTO) {
        try {
            final NewsAccessRule rule = ruleDTO.toEntity();
            final NewsAccessRule saved = newsAccessRuleRepository.save(rule);

            if (VisibilityType.RESTRICTED == saved.getVisibility()) {
                final List<Member> members = memberRepository.findByIdIn(saved.getVisibleToMemberIds()).stream()
                        .toList();
                final NewsAccessRuleDTO dto = saved.toDto();
                dto.setVisibleToMember(members);
                return Result.ok(dto);
            } else {
                return Result.ok(saved.toDto());
            }
        } catch (Exception e) {
            return Result.err("儲存失敗：" + e.getMessage());
        }
    }

    @Override
    public Result<List<NewsAccessRuleDTO>, String> findAllByMemberId(String memberId) {
        List<NewsAccessRule> rules = newsAccessRuleRepository.findByCreatedByOrVisibilityIn(memberId, List.of(VisibilityType.PUBLIC, VisibilityType.PRIVATE));

        final Set<String> memberIds = new HashSet<>();
        for (NewsAccessRule rule : rules) {
            final Set<String> visibleToMemberIds = rule.getVisibleToMemberIds();
            if (!visibleToMemberIds.isEmpty())
                memberIds.addAll(visibleToMemberIds);
        }

        final Map<String, Member> memberMap = memberRepository.findByIdIn(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));

        final List<NewsAccessRuleDTO> ruleDTOList = rules.stream()
                .map(r -> {
                    final NewsAccessRuleDTO dto = r.toDto();
                    final Set<String> visibleToMemberIds = dto.getVisibleToMemberIds();
                    if (visibleToMemberIds != null && !visibleToMemberIds.isEmpty()) {
                        List<Member> visibleMembers = visibleToMemberIds.stream()
                                .map(memberMap::get)
                                .filter(Objects::nonNull)
                                .toList();
                        dto.setVisibleToMember(visibleMembers);
                    }
                    return dto;
                }).toList();

        if (rules.size() == 0) {
            return Result.ok(Collections.emptyList());
        } else {
            return Result.ok(ruleDTOList);
        }
    }

    @Override
    public Result<String, String> deleteRule(String memberId, String ruleId) {
        Optional<NewsAccessRule> optionalRule = newsAccessRuleRepository.findById(ruleId);

        if (optionalRule.isEmpty()) {
            return Result.err("找不到對應的 Rule 資料");
        }

        NewsAccessRule rule = optionalRule.get();

        if (!rule.getCreatedBy().equals(memberId)) {
            return Result.err("無權限刪除此權限規則");
        }

        if (rule.getVisibility() == VisibilityType.PUBLIC || rule.getVisibility() == VisibilityType.PRIVATE) {
            return Result.err("無法刪除系統預設的 PUBLIC / PRIVATE 規則");
        }

        newsAccessRuleRepository.deleteById(ruleId);
        return Result.ok("刪除成功");
    }

    @Override
    public Result<NewsAccessRuleDTO, String> edit(NewsAccessRuleDTO ruleDTO) {
        try {
            Optional<NewsAccessRule> optionalRule = newsAccessRuleRepository.findById(ruleDTO.getId());

            if (optionalRule.isEmpty()) {
                return Result.err("找不到對應的 Rule 資料");
            }

            final VisibilityType visibility = ruleDTO.getVisibility();

            NewsAccessRule rule = optionalRule.get();
            if (StringUtils.isNotBlank(ruleDTO.getRuleName()))
                rule.setRuleName(ruleDTO.getRuleName());
            if (VisibilityType.GROUP.equals(visibility))
                rule.setVisibleToGroupIds(ruleDTO.getVisibleToGroupIds());
            if (VisibilityType.RESTRICTED.equals(visibility))
                rule.setVisibleToMemberIds(ruleDTO.getVisibleToMemberIds());

            final NewsAccessRule saved = newsAccessRuleRepository.save(rule);
            if (VisibilityType.RESTRICTED == saved.getVisibility()) {
                final List<Member> members = memberRepository.findByIdIn(saved.getVisibleToMemberIds()).stream()
                        .toList();
                final NewsAccessRuleDTO dto = saved.toDto();
                dto.setVisibleToMember(members);
                return Result.ok(dto);
            } else {
                return Result.ok(saved.toDto());
            }
        } catch (Exception e) {
            return Result.err("儲存失敗：" + e.getMessage());
        }
    }

    @Override
    public List<NewsAccessRuleDTO> findByVisibilityIn(List<VisibilityType> types) {
        final List<NewsAccessRule> rules = newsAccessRuleRepository.findByVisibilityIn(types);
        return rules.stream().map(NewsAccessRule::toDto).toList();
    }

    @Override
    public Result<NewsAccessRuleDTO, String> findById(String ruleId) {
        Optional<NewsAccessRule> optional = newsAccessRuleRepository.findById(ruleId);
        if (optional.isPresent()) {
            final NewsAccessRule newsAccessRule = optional.get();
            if (VisibilityType.RESTRICTED == newsAccessRule.getVisibility()) {
                final List<Member> members = memberRepository.findByIdIn(newsAccessRule.getVisibleToMemberIds()).stream()
                        .toList();
                final NewsAccessRuleDTO dto = newsAccessRule.toDto();
                dto.setVisibleToMember(members);
                return Result.ok(dto);
            } else {
                return Result.ok(newsAccessRule.toDto());
            }
        } else {
            return Result.err("找不到對應的權限資料");
        }
    }
}
