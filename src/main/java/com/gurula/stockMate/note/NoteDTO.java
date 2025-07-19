package com.gurula.stockMate.note;

import com.gurula.stockMate.study.VersionType;

import java.util.List;

public class NoteDTO {
    private String id;
    private String memberId;
    private String layoutId;
    private String title;   // 標題
    private String content; // 內容
    private List<String> tags;  // 標籤（例如 ["投資", "技術分析"]）
    private VersionType versionType;
    private String layoutName;
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

    public VersionType getVersionType() {
        return versionType;
    }

    public void setVersionType(VersionType versionType) {
        this.versionType = versionType;
    }

    public String getLayoutName() {
        return layoutName;
    }

    public void setLayoutName(String layoutName) {
        this.layoutName = layoutName;
    }

    protected Note toEntity() {
        Note note = new Note();
        note.setMemberId(this.memberId);
        note.setLayoutId(this.layoutId);
        note.setTitle(this.title);
        note.setContent(this.content);
        note.setTags(this.tags);
        note.setCreatedAt(System.currentTimeMillis());
        return note;
    }
}
