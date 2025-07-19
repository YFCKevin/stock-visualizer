
package com.gurula.stockMate.study;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "study_content_item")
public class StudyContentItem {
    @Id
    private String id;
    private String studyId;
    private ContentType contentType;
    private String contentId;
    private int sortOrder;

    public StudyContentItem() {
    }

    public StudyContentItem(String studyId, ContentType contentType, String contentId, int sortOrder) {
        this.studyId = studyId;
        this.contentType = contentType;
        this.contentId = contentId;
        this.sortOrder = sortOrder;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudyId() {
        return this.studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public ContentType getContentType() {
        return this.contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    public String getContentId() {
        return this.contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public int getSortOrder() {
        return this.sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String toString() {
        return "StudyContentItem{contentId='" + this.contentId + "'}";
    }
}
