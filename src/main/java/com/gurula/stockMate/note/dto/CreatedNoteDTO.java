package com.gurula.stockMate.note.dto;

import com.gurula.stockMate.note.Note;
import com.gurula.stockMate.study.VersionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "使用者新增/修改的筆記配置資料")
public class CreatedNoteDTO {

    @Schema(description = "要修改的note ID")
    private String id;

    @Schema(description = "member ID", accessMode = Schema.AccessMode.READ_ONLY)
    private String memberId;

    @Schema(description = "指定的layout ID")
    private String layoutId;

    @Schema(description = "筆記標題", example = "我的筆記標題")
    private String title;   // 標題

    @Schema(description = "筆記內容", example = "我的筆記內容")
    private String content; // 內容

    @Schema(
            description = "筆記分類",
            example = "[\"投資\", \"技術分析\"]"
    )
    private List<String> tags;  // 標籤（例如 ["投資", "技術分析"]）

    @Schema(description = "建立時間（timestamp）", accessMode = Schema.AccessMode.READ_ONLY)
    private long createdAt;

    @Schema(description = "更新時間（timestamp）", accessMode = Schema.AccessMode.READ_ONLY)
    private long updatedAt;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Note toEntity() {
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
