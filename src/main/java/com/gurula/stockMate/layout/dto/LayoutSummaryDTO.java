package com.gurula.stockMate.layout.dto;

import com.gurula.stockMate.layout.Layout;

public class LayoutSummaryDTO {
    private String id;
    private String name;
    private String desc;
    private String symbolId;
    private long createAt;
    private long updateAt;

    public LayoutSummaryDTO() {
    }

    public LayoutSummaryDTO(String id, String name, String desc, String symbolId, long createAt, long updateAt) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.symbolId = symbolId;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public static LayoutSummaryDTO construct(Layout layout) {
        return new LayoutSummaryDTO(
                layout.getId(),
                layout.getName(),
                layout.getDesc(),
                layout.getSymbolId(),
                layout.getCreateAt(),
                layout.getUpdateAt()
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
