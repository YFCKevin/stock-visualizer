package com.gurula.stockMate.study;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "study_note")
public class StudyNote {
    @Id
    private String id;
    private String studyId;
    private String noteId;
    private boolean syncEnabled;
    private String currentVersionId;

    public StudyNote() {
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

    public String getNoteId() {
        return this.noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public boolean isSyncEnabled() {
        return this.syncEnabled;
    }

    public void setSyncEnabled(boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }

    public String getCurrentVersionId() {
        return this.currentVersionId;
    }

    public void setCurrentVersionId(String currentVersionId) {
        this.currentVersionId = currentVersionId;
    }
}
