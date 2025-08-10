package com.gurula.stockMate.newsAccessRule.dto;

import com.gurula.stockMate.newsAccessRule.NewsAccessRule;
import com.gurula.stockMate.newsAccessRule.validator.ValidVisibility;
import com.gurula.stockMate.newsAccessRule.VisibilityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@ValidVisibility
@Schema(description = "使用者建立的新聞權限規則配置資料")
public class CreatedNewsAccessRuleDTO {

    @NotBlank(message = "權限規則名稱不能為空")
    @Schema(description = "權限規則名稱", example = "只給好友看", required = true)
    private String ruleName;                      // 權限規則名稱，如「只給好友看」、「所有人可見」

    @NotNull(message = "權限規則範圍不能為空")
    @Schema(description = "權限規則範圍 (PUBLIC, PRIVATE, RESTRICTED, GROUP, ROLE)", example = "PRIVATE", required = true)
    private VisibilityType visibility;            // PUBLIC, PRIVATE, RESTRICTED, GROUP, ROLE

    @Schema(description = "支援對象 (當 visibility 選取 RESTRICTED 時必填)")
    private Set<String> visibleToMemberIds;      // visibility = RESTRICTED 時使用

    @Schema(description = "支援群組 (當 visibility 選取 GROUP 時必填)")
    private Set<String> visibleToGroupIds;       // 支援群組

    @Schema(description = "支援角色 (當 visibility 選取 ROLE 時必填)")
    private Set<String> visibleToRoleIds;        // 支援角色

    @Schema(description = "權限規則創建者 ID (memberId)", accessMode = Schema.AccessMode.READ_ONLY)
    private String createdBy;                    // memberId

    @Schema(description = "建立時間（timestamp）", accessMode = Schema.AccessMode.READ_ONLY)
    private long createdAt;

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

    public NewsAccessRule toEntity() {
        NewsAccessRule entity = new NewsAccessRule();
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
