package com.gurula.stockMate.study;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "study_note_version")
public class StudyNoteVersion {
    @Id
    private String id;
    private String studyNoteId;
    private String title;
    private String content;
    private String memberId;
    private VersionType versionType;
    private long createdAt;

    public StudyNoteVersion() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudyNoteId() {
        return this.studyNoteId;
    }

    public void setStudyNoteId(String studyNoteId) {
        this.studyNoteId = studyNoteId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMemberId() {
        return this.memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public VersionType getVersionType() {
        return this.versionType;
    }

    public void setVersionType(VersionType versionType) {
        this.versionType = versionType;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
