package com.gurula.stockMate.note;

import com.gurula.stockMate.note.dto.NoteDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "note")
public class Note {
    @Id
    private String id;
    private String memberId;
    private String layoutId;
    private String title;   // 標題
    private String content; // 內容
    private List<String> tags;  // 標籤（例如 ["投資", "技術分析"]）
    private long createdAt;
    private long updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(String layoutId) {
        this.layoutId = layoutId;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public NoteDTO toDto() {
        NoteDTO dto = new NoteDTO();
        dto.setId(this.id);
        dto.setMemberId(this.memberId);
        dto.setLayoutId(this.layoutId);
        dto.setTitle(this.title);
        dto.setContent(this.content);
        dto.setTags(this.tags);
        dto.setCreatedAt(this.createdAt);
        dto.setUpdatedAt(this.updatedAt);
        return dto;
    }
}
