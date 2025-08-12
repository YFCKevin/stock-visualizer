package com.gurula.stockMate.favorite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class FavoriteSymbolReorderDTO {

    @Schema(description = "建立者member ID", accessMode = Schema.AccessMode.READ_ONLY)
    private String memberId;

    @NotNull(message = "項目列表不得為空")
    @Size(min = 1, message = "至少需要一個排序項目")
    @Schema(description = "重新排序的項目列表")
    private List<ReorderItemDTO> items;

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public List<ReorderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ReorderItemDTO> items) {
        this.items = items;
    }
}
