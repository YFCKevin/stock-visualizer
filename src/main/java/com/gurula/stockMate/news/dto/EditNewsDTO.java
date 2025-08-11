package com.gurula.stockMate.news.dto;

import com.gurula.stockMate.news.MacroFactor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "使用者修改的新聞配置資料")
public class EditNewsDTO {

    @NotBlank(message = "新聞 ID 不得為空")
    @Schema(description = "要修改的新聞 ID", required = true)
    private String id;

    @NotBlank(message = "新聞標題不得為空")
    @Schema(description = "新聞標題", example = "美股收紅，那斯達克指數續創新高", required = true)
    private String title;

    @Schema(description = "新聞內容")
    private String content;

    @NotBlank(message = "新聞連結不得為空")
    @Schema(description = "新聞連結 URL", example = "https://www.sample.com", required = true)
    private String url;

    @NotNull(message = "新聞發布時間不得為空")
    @Min(0)
    @Schema(description = "新聞發布時間（Unix 時間戳記，毫秒）", example = "1752104354554", required = true)
    private long publishedAt;

    @NotNull(message = "新聞分類不能為空")
    @Size(min = 1, message = "至少選擇一個分類")
    @Schema(description = "新聞分類 (如：OIL_PRICE, INFLATION, ECONOMY)", example = "[\"OIL_PRICE\", \"INFLATION\"]", required = true)
    private List<MacroFactor> tags;

    @Schema(description = "建立時間（Unix 時間戳記，毫秒）", accessMode = Schema.AccessMode.READ_ONLY)
    private long createdAt;

    @Schema(description = "建立者member ID", accessMode = Schema.AccessMode.READ_ONLY)
    private String memberId;

    @NotBlank(message = "股市代號不得為空")
    @Schema(description = "股市代號", example = "2317.TW")
    private String symbol;

    @NotBlank(message = "權限規則不得為空")
    @Schema(description = "權限規則 ID", example = "689720e812969d38e7685cd9", required = true)
    private String accessRuleId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(long publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<MacroFactor> getTags() {
        return tags;
    }

    public void setTags(List<MacroFactor> tags) {
        this.tags = tags;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
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

    public String getAccessRuleId() {
        return accessRuleId;
    }

    public void setAccessRuleId(String accessRuleId) {
        this.accessRuleId = accessRuleId;
    }
}
