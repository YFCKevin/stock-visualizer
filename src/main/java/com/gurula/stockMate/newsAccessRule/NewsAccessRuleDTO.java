package com.gurula.stockMate.newsAccessRule;

import com.gurula.stockMate.member.Member;

import java.util.List;
import java.util.Set;

public class NewsAccessRuleDTO {
    private String id;
    private String ruleName;                      // 權限規則名稱，如「只給好友看」、「所有人可見」
    private VisibilityType visibility;            // PUBLIC, PRIVATE, RESTRICTED, GROUP
    private Set<String> visibleToMemberIds;      // visibility = RESTRICTED 時使用
    private Set<String> visibleToGroupIds;       // 支援群組
    private Set<String> visibleToRoleIds;        // 支援角色
    private String createdBy;                    // memberId
    private long createdAt;
    private List<Member> visibleToMember;        // 用來前端顯示會員資料

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

    public List<Member> getVisibleToMember() {
        return visibleToMember;
    }

    public void setVisibleToMember(List<Member> visibleToMember) {
        this.visibleToMember = visibleToMember;
    }

    protected NewsAccessRule toEntity() {
        NewsAccessRule entity = new NewsAccessRule();
        entity.setId(this.id);
        entity.setRuleName(this.ruleName);
        entity.setVisibility(this.visibility);
        entity.setVisibleToMemberIds(this.visibleToMemberIds);
        entity.setVisibleToGroupIds(this.visibleToGroupIds);
        entity.setVisibleToRoleIds(this.visibleToRoleIds);
        entity.setCreatedBy(this.createdBy);
        entity.setCreatedAt(System.currentTimeMillis());
        return entity;
    }
}
