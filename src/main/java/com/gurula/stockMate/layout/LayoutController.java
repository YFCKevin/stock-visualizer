package com.gurula.stockMate.layout;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.layout.dto.*;
import com.gurula.stockMate.member.Member;
import com.gurula.stockMate.member.MemberContext;
import com.gurula.stockMate.ohlc.OhlcData;
import com.gurula.stockMate.ohlc.OhlcDataDTO;
import com.gurula.stockMate.ohlc.OhlcService;
import com.gurula.stockMate.symbol.Symbol;
import com.gurula.stockMate.symbol.SymbolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.gurula.stockMate.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@RestController
@RequestMapping("/layout")
@Tag(name = "Layout API", description = "股市版面")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class LayoutController {
    private final LayoutService layoutService;
    private final OhlcService ohlcService;
    private final SymbolService symbolService;

    public LayoutController(LayoutService layoutService, OhlcService ohlcService, SymbolService symbolService) {
        this.layoutService = layoutService;
        this.ohlcService = ohlcService;
        this.symbolService = symbolService;
    }


    @GetMapping
    @Operation(summary = "取得該會員的所有股市版面")
    public ResponseEntity<?> getAllLayouts() {
        final Member member = MemberContext.getMember();
        final List<LayoutSummaryDTO> allLayouts = layoutService.getAllLayouts(member.getId());
        return ResponseEntity.ok(allLayouts);
    }


    @GetMapping("/search")
    @Operation(
            summary = "搜尋該會員的股市版面",
            description = "根據關鍵字搜尋該會員所擁有的股市版面。若未提供關鍵字，則回傳該會員的所有版面。",
            parameters = {
                    @Parameter(
                            name = "keyword",
                            description = "搜尋關鍵字，例如版面名稱、股票代號、股票名稱",
                            example = "台積電 or 2330.TW"
                    )
            }
    )
    public ResponseEntity<?> searchLayouts(@RequestParam(required = false) String keyword) {
        final Member member = MemberContext.getMember();
        List<LayoutSummaryDTO> layouts = layoutService.search(member.getId(), keyword);
        return ResponseEntity.ok(layouts);
    }

    @PostMapping
    @Operation(summary = "新增一個股市版面")
    public ResponseEntity<?> create(@Valid @RequestBody CreatedLayoutDTO layoutDTO) {
        final Member member = MemberContext.getMember();
        final String symbolName = layoutDTO.getSymbol();
        final String interval = layoutDTO.getInterval();

        try {
            final Result<Layout, String> result = layoutService.constructNewLayout(member.getId(), symbolName, interval);
            if (result.isOk()) {
                Layout layout = result.unwrap();
                return ResponseEntity.ok(layout.getId());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", result.unwrapErr()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/enter")
    @Operation(
            summary = "取得股市版面資訊、股價與成交量",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "版面 ID，用於指定要查詢的版面",
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "688aed443228c779718db850"
                    ),
                    @Parameter(
                            name = "symbolName",
                            description = "股票代碼或名稱，例如 '2330.TW'",
                            in = ParameterIn.QUERY,
                            required = false,
                            example = "2330.TW"
                    )
            }
    )
    public ResponseEntity<?> enterLayout(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String symbolName
    ) {

        String interval = "1d";
        final Member member = MemberContext.getMember();
        Result<Layout, String> result = null;
        if (StringUtils.isNotBlank(id)) {
            // 有傳 id，直接根據 id 查找 layout，並且用 memberId驗證是否為該會員

            try {
                result = layoutService.findByIdAndMemberId(id, member.getId());
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                if (errorMessage.equals("找不到對應的 Layout 資料")) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", errorMessage));
                }
            }

            if (result.isOk() && StringUtils.isBlank(symbolName)) {
                Layout layout = result.unwrap();
                final String symbolId = layout.getSymbolId();
                final Symbol symbol = symbolService.findById(symbolId).get();
                symbolName = symbol.getSymbol();
                interval = layout.getInterval().getValue();
            }

        } else if (StringUtils.isNotBlank(symbolName)) {
            // 限定股號
            result = layoutService.findLatestBySymbol(symbolName, member.getId());
        }

        // 取得股市資料
        List<OhlcDataDTO> ohlcDataDTOList = ohlcService.loadOhlcData(symbolName, interval);

        try {
            if (result.isOk()) {
                Layout layout = result.unwrap();
                final LayoutDTO dto = layout.toDto();
                dto.setOhlcDataDTOList(ohlcDataDTOList);
                dto.setSymbol(symbolName);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", result.unwrapErr()));
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            if (errorMessage.equals("找不到對應的 Layout 資料")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", errorMessage));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }
    }

    @Operation(summary = "儲存股市版面")
    @PostMapping("/save")
    public ResponseEntity<?> save(@Valid @RequestBody StoredLayoutDTO layoutDTO) {
        final Member member = MemberContext.getMember();
        layoutDTO.setMemberId(member.getId());

        try {
            Result<Layout, String> result = layoutService.save(layoutDTO);
            if (result.isOk()) {
                Layout layout = result.unwrap();
                LayoutSummaryDTO dto = LayoutSummaryDTO.construct(layout);
                return ResponseEntity.ok(dto);
            } else {
                String errorMessage = result.unwrapErr();
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", errorMessage));
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "修改股市版面")
    @PatchMapping("/edit")
    public ResponseEntity<?> edit(@Valid @RequestBody EditLayoutDTO layoutDTO) {
        final Member member = MemberContext.getMember();
        layoutDTO.setMemberId(member.getId());

        try {
            Result<Layout, String> result = layoutService.edit(layoutDTO);
            if (result.isOk()) {
                Layout layout = result.unwrap();
                LayoutSummaryDTO dto = LayoutSummaryDTO.construct(layout);
                return ResponseEntity.ok(dto);
            } else {
                String errorMessage = result.unwrapErr();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", errorMessage));
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }


    }

    @Operation(summary = "刪除股市版面")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @Parameter(description = "要刪除的layout ID")
            @PathVariable(name = "id") String id
    ) {
        final Member member = MemberContext.getMember();
        Map<String, Object> response = new HashMap<>();

        try {
            Result<String, String> result = layoutService.delete(id);
            if (result.isOk()) {
                response.put("message", result.unwrap());
                return ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            } else {
                String errorMessage = result.unwrapErr();
                response.put("error", errorMessage);
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            response.put("error", errorMessage);

            if (errorMessage.equals("找不到對應的 Layout 資料")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }

    @Operation(summary = "複製股市版面")
    @PostMapping("/copy/{layoutId}")
    public ResponseEntity<?> copy(
            @Parameter(description = "要複製的layout ID")
            @PathVariable(name = "layoutId") String layoutId
    ) {
        final Member member = MemberContext.getMember();

        try {
            Result<Layout, String> result = layoutService.copyLayout(layoutId, member.getId());
            if (result.isOk()) {
                Layout layout = result.unwrap();
                return ResponseEntity.ok(layout.getId());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", result.unwrapErr()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }


    }
}
