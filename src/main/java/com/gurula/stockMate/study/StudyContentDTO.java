
package com.gurula.stockMate.study;

import java.util.List;

public class StudyContentDTO {
    private String title;
    private String comment;
    private String id;
    private ContentType type;
    private Object data;
    private int sortOrder;
    private boolean syncEnabled;

    public StudyContentDTO() {
    }

    public StudyContentDTO(ContentType type, List<Object> data) {
        this.type = type;
        this.data = data;
    }

    public ContentType getType() {
        return this.type;
    }

    public void setType(ContentType type) {
        this.type = type;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getSortOrder() {
        return this.sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isSyncEnabled() {
        return syncEnabled;
    }

    public void setSyncEnabled(boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }
}
