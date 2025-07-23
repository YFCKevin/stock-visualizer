package com.gurula.stockMate.study;

import com.gurula.stockMate.layout.dto.LayoutDTO;
import com.gurula.stockMate.ohlc.IntervalType;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(
        collection = "study_layout_version"
)
public class StudyLayoutVersion {
    @Id
    private String id;
    private String studyLayoutId;
    private String name;
    private String desc;
    private IntervalType interval;
    private Map<String, Object> userSettings = new HashMap<>();
    private String memberId;
    private String symbolId;
    private VersionType versionType;
    private long createdAt;

    public StudyLayoutVersion() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudyLayoutId() {
        return this.studyLayoutId;
    }

    public void setStudyLayoutId(String studyLayoutId) {
        this.studyLayoutId = studyLayoutId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public IntervalType getInterval() {
        return this.interval;
    }

    public void setInterval(IntervalType interval) {
        this.interval = interval;
    }

    public Map<String, Object> getUserSettings() {
        return this.userSettings;
    }

    public void setUserSettings(Map<String, Object> userSettings) {
        this.userSettings = userSettings;
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

    public String getSymbolId() {
        return symbolId;
    }

    public void setSymbolId(String symbolId) {
        this.symbolId = symbolId;
    }

    public LayoutDTO toDto() {
        LayoutDTO dto = new LayoutDTO();
        dto.setId(this.id);
        dto.setMemberId(this.memberId);
        dto.setDesc(this.desc);
        dto.setName(this.name);
        dto.setInterval(this.interval.getValue());
        dto.setSymbolId(this.symbolId);
        dto.setUserSettings(this.userSettings);
        dto.setCreateAt(this.getCreatedAt());
        dto.setVersionType(this.versionType);
        return dto;
    }
}
