package com.gurula.stockMate.newsAccessRule;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.newsAccessRule.dto.CreatedNewsAccessRuleDTO;
import com.gurula.stockMate.newsAccessRule.dto.EditNewsAccessRuleDTO;
import com.gurula.stockMate.newsAccessRule.dto.NewsAccessRuleDTO;

import java.util.List;

public interface NewsAccessRuleService {
    Result<NewsAccessRuleDTO, String> save(CreatedNewsAccessRuleDTO  ruleDTO);

    Result<List<NewsAccessRuleDTO>, String> findAllByMemberId(String memberId);

    Result<String, String> deleteRule(String memberId, String ruleId);

    Result<NewsAccessRuleDTO, String> edit(EditNewsAccessRuleDTO ruleDTO);

    List<NewsAccessRuleDTO> findByVisibilityIn(List<VisibilityType> aPublic);

    Result<NewsAccessRuleDTO, String> findById(String ruleId);
}
