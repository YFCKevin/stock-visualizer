package com.gurula.stockMate.layout;

import com.gurula.stockMate.ohlc.IntervalType;
import com.gurula.stockMate.layout.dto.LayoutDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "layout")
public class Layout {
    @Id
    private String id;
    private String memberId;
    private String name;
    private String desc;
    private IntervalType interval;
    private String symbolId;
    private Map<String, Object> userSettings = new HashMap<>();
    private long createAt;
    private long updateAt;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public IntervalType getInterval() {
        return interval;
    }

    public void setInterval(IntervalType interval) {
        this.interval = interval;
    }

    public String getSymbolId() {
        return symbolId;
    }

    public void setSymbolId(String symbolId) {
        this.symbolId = symbolId;
    }

    public long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }

    public long getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(long updateAt) {
        this.updateAt = updateAt;
    }

    public Map<String, Object> getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(Map<String, Object> userSettings) {
        this.userSettings = userSettings;
    }

    public LayoutDTO toDto() {
        LayoutDTO dto = new LayoutDTO();
        dto.setId(this.id);
        dto.setSymbolId(this.symbolId);
        dto.setMemberId(this.memberId);
        dto.setDesc(this.desc);
        dto.setName(this.name);
        dto.setInterval(this.interval.getValue());
        dto.setUserSettings(this.userSettings);
        dto.setCreateAt(this.getCreateAt());
        dto.setUpdateAt(this.getUpdateAt());
        return dto;
    }
}
