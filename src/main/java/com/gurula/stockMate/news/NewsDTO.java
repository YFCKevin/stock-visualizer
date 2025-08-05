package com.gurula.stockMate.news;

import com.gurula.stockMate.newsAccessRule.VisibilityType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public class NewsDTO {
    @Schema(description = "新聞 ID")
    private String id;

    @NotBlank
    @Schema(description = "新聞標題")
    private String title;

    @Schema(description = "新聞內容")
    private String content;

    @NotBlank
    @Schema(description = "新聞原始連結 URL")
    private String url;

    @NotNull
    @Min(0)
    @Schema(description = "發布時間（Unix 時間戳記，毫秒）")
    private long publishedAt;

    @NotNull(message = "分類不能為空")
    @Size(min = 1, message = "至少選擇一個分類")
    @Schema(description = "新聞分類 (如：OIL_PRICE、INFLATION、ECONOMY")
    private List<MacroFactor> tags;

    @Schema(description = "建立時間（Unix 時間戳記，毫秒）")
    private long createdAt;

    @Schema(description = "建立者會員 ID")
    private String memberId;

    @NotBlank
    @Schema(description = "股市標的 ID")
    private String symbolId;

    @NotBlank
    @Schema(description = "權限規則 ID")
    private String accessRuleId;

    @NotNull
    @Schema(description = "可見性設定（如：PUBLIC、PRIVATE、RESTRICTED）")
    private VisibilityType visibility;

    @Schema(description = "RESTRICTED 權限下可見會員 ID 清單")
    private List<String> selectedVisibleMembers;

    @Schema(description = "Tag 的中文標籤，用於顯示用途")
    private List<String> tagLabels;

    @Schema(description = "是否啟用同步功能")
    private boolean syncEnabled;

    public List<String> getTagLabels() {
        return tagLabels;
    }

    public void setTagLabels(List<String> tagLabels) {
        this.tagLabels = tagLabels;
    }

    public String getAccessRuleId() {
        return accessRuleId;
    }

    public void setAccessRuleId(String accessRuleId) {
        this.accessRuleId = accessRuleId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(long publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<MacroFactor> getTags() {
        return tags;
    }

    public void setTags(List<MacroFactor> tags) {
        this.tags = tags;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getSymbolId() {
        return symbolId;
    }

    public void setSymbolId(String symbolId) {
        this.symbolId = symbolId;
    }

    public List<String> getSelectedVisibleMembers() {
        return selectedVisibleMembers;
    }

    public void setSelectedVisibleMembers(List<String> selectedVisibleMembers) {
        this.selectedVisibleMembers = selectedVisibleMembers;
    }

    public VisibilityType getVisibility() {
        return visibility;
    }

    public void setVisibility(VisibilityType visibility) {
        this.visibility = visibility;
    }

    public boolean isSyncEnabled() {
        return syncEnabled;
    }

    public void setSyncEnabled(boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }

    public News toEntity() {
        News news = new News();
        news.setId(this.id);
        news.setTitle(this.title);
        news.setContent(this.content);
        news.setUrl(this.url);
        news.setPublishedAt(this.publishedAt);
        news.setTags(this.tags);
        news.setCreatedAt(System.currentTimeMillis());
        news.setMemberId(this.memberId);
        news.setAccessRuleId(this.accessRuleId);
        return news;
    }
}
