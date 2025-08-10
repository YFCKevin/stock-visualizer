package com.gurula.stockMate.note.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "使用者修改筆記標題的配置資料")
public class EditTitleNoteDTO {

    @Schema(description = "要修改的note ID")
    private String id;

    @Schema(description = "member ID", accessMode = Schema.AccessMode.READ_ONLY)
    private String memberId;

    @Schema(description = "筆記標題", example = "修改後的筆記標題")
    private String title;

    @Schema(description = "更新時間（timestamp）", accessMode = Schema.AccessMode.READ_ONLY)
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
