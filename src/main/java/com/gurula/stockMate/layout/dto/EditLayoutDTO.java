package com.gurula.stockMate.layout.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Schema(description = "使用者修改的版面配置資料")
public class EditLayoutDTO {

    @Schema(description = "layout ID")
    private String id;

    @Schema(description = "member ID", accessMode = Schema.AccessMode.READ_ONLY)
    private String memberId;

    @Schema(description = "版面配置描述", example = "這是我的自訂股票儀表板")
    private String desc;

    @Schema(description = "版面名稱", example = "我的投資儀表板")
    private String name;

    @Schema(description = "股票圖表的時間週期（日、週、月等）", example = "1d")
    private String interval;

    @Schema(description = "更新時間（timestamp）", accessMode = Schema.AccessMode.READ_ONLY)
    private String updateAt;

    @Schema(description = "使用者的自訂設定")
    private Map<String, Object> userSettings = new HashMap<>();

    @Schema(description = "股票代號", example = "2331.TW", required = true)
    private String symbol;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
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

    public Map<String, Object> getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(Map<String, Object> userSettings) {
        this.userSettings = userSettings;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
