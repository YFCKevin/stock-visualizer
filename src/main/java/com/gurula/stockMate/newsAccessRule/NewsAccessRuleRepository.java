package com.gurula.stockMate.newsAccessRule;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;

public interface NewsAccessRuleRepository extends MongoRepository<NewsAccessRule, String> {
    List<NewsAccessRule> findByCreatedByOrVisibilityIn(String memberId, List<VisibilityType> types);

    List<NewsAccessRule> findByIdIn(Set<String> accessRuleIds);

    List<NewsAccessRule> findByVisibilityIn(List<VisibilityType> types);
}
