package com.gurula.stockMate.news;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "news")
public class News {
    @Id
    private String id;
    private String title;
    private String content;
    private String url;
    private long publishedAt;   // 發布時間
    private List<MacroFactor> tags; // 閱讀新聞邏輯架構分類
    private long createdAt;
    private String memberId;
    private String accessRuleId;    // 指向的權限規則

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

    public String getAccessRuleId() {
        return accessRuleId;
    }

    public void setAccessRuleId(String accessRuleId) {
        this.accessRuleId = accessRuleId;
    }

    public NewsDTO toDto() {
        NewsDTO newsDTO = new NewsDTO();
        newsDTO.setId(this.id);
        newsDTO.setTitle(this.title);
        newsDTO.setContent(this.content);
        newsDTO.setUrl(this.url);
        newsDTO.setPublishedAt(this.publishedAt);
        newsDTO.setTags(this.tags);
        newsDTO.setCreatedAt(this.createdAt);
        newsDTO.setMemberId(this.memberId);

        if (this.tags != null) {
            List<String> zhLabels = this.tags.stream()
                    .map(MacroFactor::getZhLabel)
                    .toList();
            newsDTO.setTagLabels(zhLabels);
        } else {
            newsDTO.setTagLabels(List.of());
        }

        return newsDTO;
    }

    @Override
    public String toString() {
        return "News{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", url='" + url + '\'' +
                ", publishedAt=" + publishedAt +
                ", tags=" + tags +
                ", createdAt=" + createdAt +
                ", memberId='" + memberId + '\'' +
                ", accessRuleId='" + accessRuleId + '\'' +
                '}';
    }
}
