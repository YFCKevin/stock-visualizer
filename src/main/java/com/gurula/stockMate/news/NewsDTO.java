package com.gurula.stockMate.news;

import com.gurula.stockMate.newsAccessRule.VisibilityType;

import java.util.List;

public class NewsDTO {
    private String id;
    private String title;
    private String content;
    private String url;
    private long publishedAt;   // 發布時間
    private List<MacroFactor> tags; // 閱讀新聞邏輯架構分類
    private long createdAt;
    private String memberId;
    private String symbolId;    // 股市編號
    private String accessRuleId;    // 指向的權限規則
    private VisibilityType visibility;  // 前端用來傳送用
    private List<String> selectedVisibleMembers;    // RESTRICTED 權限選擇的會員 id
    private List<String> tagLabels;   // 用來顯示 tag 的中文字

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
