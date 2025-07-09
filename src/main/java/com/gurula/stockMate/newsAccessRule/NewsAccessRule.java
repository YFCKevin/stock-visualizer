package com.gurula.stockMate.newsAccessRule;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "news_access_rule")
public class NewsAccessRule {
    @Id
    private String id;
    private String ruleName;                      // 權限規則名稱，如「只給好友看」、「所有人可見」
    private VisibilityType visibility;            // PUBLIC, PRIVATE, RESTRICTED, GROUP
    private Set<String> visibleToMemberIds;      // visibility = RESTRICTED 時使用
    private Set<String> visibleToGroupIds;       // 支援群組
    private Set<String> visibleToRoleIds;        // 支援角色
    private String createdBy;                     // memberId
    private long createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public VisibilityType getVisibility() {
        return visibility;
    }

    public void setVisibility(VisibilityType visibility) {
        this.visibility = visibility;
    }

    public Set<String> getVisibleToMemberIds() {
        return visibleToMemberIds;
    }

    public void setVisibleToMemberIds(Set<String> visibleToMemberIds) {
        this.visibleToMemberIds = visibleToMemberIds;
    }

    public Set<String> getVisibleToGroupIds() {
        return visibleToGroupIds;
    }

    public void setVisibleToGroupIds(Set<String> visibleToGroupIds) {
        this.visibleToGroupIds = visibleToGroupIds;
    }

    public Set<String> getVisibleToRoleIds() {
        return visibleToRoleIds;
    }

    public void setVisibleToRoleIds(Set<String> visibleToRoleIds) {
        this.visibleToRoleIds = visibleToRoleIds;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public NewsAccessRuleDTO toDto() {
        NewsAccessRuleDTO dto = new NewsAccessRuleDTO();
        dto.setId(this.id);
        dto.setRuleName(this.ruleName);
        dto.setVisibility(this.visibility);
        dto.setVisibleToMemberIds(this.visibleToMemberIds);
        dto.setVisibleToGroupIds(this.visibleToGroupIds);
        dto.setVisibleToRoleIds(this.visibleToRoleIds);
        dto.setCreatedBy(this.createdBy);
        dto.setCreatedAt(this.createdAt);
        return dto;
    }
}
