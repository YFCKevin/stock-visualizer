package com.gurula.stockMate.study;

import java.util.List;

public class StudyContentReorderDTO {
    private String studyId;
    private List<StudyContentOrderItemDTO> reorderedItems;

    public StudyContentReorderDTO() {
    }

    public String getStudyId() {
        return this.studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public List<StudyContentOrderItemDTO> getReorderedItems() {
        return this.reorderedItems;
    }

    public void setReorderedItems(List<StudyContentOrderItemDTO> reorderedItems) {
        this.reorderedItems = reorderedItems;
    }
}
