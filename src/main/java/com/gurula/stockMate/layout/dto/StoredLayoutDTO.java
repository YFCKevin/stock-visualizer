package com.gurula.stockMate.layout.dto;

import com.gurula.stockMate.layout.Layout;
import com.gurula.stockMate.ohlc.IntervalType;
import com.gurula.stockMate.symbol.Symbol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.Map;

@Schema(description = "使用者儲存版面的配置資料")
public class StoredLayoutDTO {

    @Schema(description = "member ID", accessMode = Schema.AccessMode.READ_ONLY)
    private String memberId;

    @Schema(description = "版面配置描述", example = "這是我的自訂股票儀表板")
    private String desc;

    @NotBlank(message = "版面名稱不得為空")
    @Schema(description = "版面名稱", example = "我的投資儀表板", required = true)
    private String name;

    @NotBlank(message = "股票圖表的時間週期不得為空")
    @Schema(description = "股票圖表的時間週期（日、週、月等）", example = "1d", required = true)
    private String interval;

    @Schema(description = "使用者的自訂設定")
    private Map<String, Object> userSettings = new HashMap<>();

    @NotBlank(message = "股票 ID 不得為空")
    @Schema(description = "股票 ID", required = true)
    private String symbol;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
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

    public Layout toEntity(Symbol symbol) {
        Layout layout = new Layout();
        layout.setMemberId(this.memberId);
        layout.setDesc(this.desc);
        layout.setName(this.name);
        layout.setInterval(IntervalType.fromValue(this.interval));
        layout.setUserSettings(this.userSettings);
        layout.setSymbolId(symbol.getId());
        return layout;
    }
}
