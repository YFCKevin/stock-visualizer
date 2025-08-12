package com.gurula.stockMate.favorite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class CreateFavoriteSymbolDTO {

    @NotBlank(message = "股票代號 ID 不得為空")
    @Schema(description = "股票代號 ID", example = "6860e697c0c3cb7baa2da2ee")
    private String symbolId;

    public String getSymbolId() {
        return symbolId;
    }

    public void setSymbolId(String symbolId) {
        this.symbolId = symbolId;
    }
}
