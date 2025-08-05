package com.gurula.stockMate.study;

import io.swagger.v3.oas.annotations.media.Schema;

public class StudyDTO {
    private String id;
    @Schema(description = "研究報告標題", example = "2025 AI 趨勢研究")
    private String title;
    @Schema(description = "詳細描述", example = "這份報告分析了 AI 的應用前景...")
    private String desc;
    private long createdAt;
    private long updatedAt;
    private String memberId;
    private boolean archive;

    public StudyDTO() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getMemberId() {
        return this.memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }
}
