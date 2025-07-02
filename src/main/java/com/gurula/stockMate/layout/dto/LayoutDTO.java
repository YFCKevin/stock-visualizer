package com.gurula.stockMate.layout.dto;

import com.gurula.stockMate.ohlc.IntervalType;
import com.gurula.stockMate.layout.Layout;
import com.gurula.stockMate.ohlc.OhlcDataDTO;
import com.gurula.stockMate.symbol.Symbol;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LayoutDTO {
    private String id;
    private String memberId;
    private String name;
    private String desc;
    private String interval;
    private String symbol;
    private String symbolId;
    private Map<String, Object> userSettings = new HashMap<>();
    private List<OhlcDataDTO> ohlcDataDTOList;
    private String createAt;
    private String updateAt;

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        if (createAt > 0) {
            this.createAt = Instant.ofEpochMilli(createAt)
                    .atZone(ZoneId.of("Asia/Taipei"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    public String getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(long updateAt) {
        if (updateAt > 0) {
            this.updateAt = Instant.ofEpochMilli(updateAt)
                    .atZone(ZoneId.of("Asia/Taipei"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

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

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getSymbolId() {
        return symbolId;
    }

    public void setSymbolId(String symbolId) {
        this.symbolId = symbolId;
    }

    public Map<String, Object> getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(Map<String, Object> userSettings) {
        this.userSettings = userSettings;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<OhlcDataDTO> getOhlcDataDTOList() {
        return ohlcDataDTOList;
    }

    public void setOhlcDataDTOList(List<OhlcDataDTO> ohlcDataDTOList) {
        this.ohlcDataDTOList = ohlcDataDTOList;
    }

    public Layout toEntity(Symbol symbol) {
        Layout layout = new Layout();
        layout.setMemberId(this.memberId);
        layout.setDesc(this.desc);
        layout.setName(this.name);
        layout.setInterval(IntervalType.fromValue(this.interval));
        layout.setCreateAt(System.currentTimeMillis());
        layout.setUserSettings(this.userSettings);
        layout.setSymbolId(symbol.getId());
        return layout;
    }
}
