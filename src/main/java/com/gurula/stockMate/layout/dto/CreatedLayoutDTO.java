package com.gurula.stockMate.layout.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;


@Schema(description = "使用者建立的版面配置資料")
public class CreatedLayoutDTO {

    @NotBlank
    @Schema(description = "股票圖表的時間週期（日、週、月等）", example = "1d", required = true)
    private String interval;

    @NotBlank
    @Schema(description = "股票代號", required = true)
    private String symbol;

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
