package com.gurula.stockMate.study;

public class ImportDTO {
    private String selfId;
    private String contentId;
    private ContentType contentType;
    private boolean syncEnabled;

    public ImportDTO() {
    }

    public ContentType getContentType() {
        return this.contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getSelfId() {
        return this.selfId;
    }

    public void setSelfId(String selfId) {
        this.selfId = selfId;
    }

    public String getContentId() {
        return this.contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public boolean isSyncEnabled() {
        return this.syncEnabled;
    }

    public void setSyncEnabled(boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }
}

