package com.gurula.stockMate.favorite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ReorderItemDTO {
    @NotBlank(message = "股票代號 ID 不得為空")
    @Schema(description = "股票代號 ID")
    private String symbolId;

    @Min(value = 0, message = "排序順序不得小於 0")
    @Schema(description = "排列順序")
    private int sortOrder;

    public String getSymbolId() {
        return symbolId;
    }

    public void setSymbolId(String symbolId) {
        this.symbolId = symbolId;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
