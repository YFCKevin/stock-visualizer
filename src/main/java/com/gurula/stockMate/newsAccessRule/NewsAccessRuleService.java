package com.gurula.stockMate.newsAccessRule;

import com.gurula.stockMate.exception.Result;

import java.util.List;

public interface NewsAccessRuleService {
    Result<NewsAccessRuleDTO, String> save(NewsAccessRuleDTO ruleDTO);

    Result<List<NewsAccessRuleDTO>, String> findAllByMemberId(String memberId);

    Result<String, String> deleteRule(String memberId, String ruleId);

    Result<NewsAccessRuleDTO, String> edit(NewsAccessRuleDTO ruleDTO);

    List<NewsAccessRuleDTO> findByVisibilityIn(List<VisibilityType> aPublic);

    Result<NewsAccessRuleDTO, String> findById(String ruleId);
}
