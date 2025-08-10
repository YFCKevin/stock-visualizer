package com.gurula.stockMate.ohlc;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.member.Member;
import com.gurula.stockMate.member.MemberContext;
import com.gurula.stockMate.symbol.Symbol;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.gurula.stockMate.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@RestController
@RequestMapping("/ohlc")
@Tag(name = "OHLC API", description = "股價與成交量資訊")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class OhlcController {
    private final OhlcService ohlcService;

    public OhlcController(OhlcService ohlcService) {
        this.ohlcService = ohlcService;
    }

    @Operation(summary = "取得股票的股價與成交量資料")
    @GetMapping("/{symbolName}/{interval}")
    public ResponseEntity<?> loadOhlcData(
            @Parameter(description = "股票代號", example = "2317.TW", required = true)
            @PathVariable(name = "symbolName") String symbolName,
            @Parameter(description = "股票圖表的時間週期（日、週、月等）", example = "1d", required = true)
            @PathVariable(name = "interval") String interval
    ) {
        final Member member = MemberContext.getMember();
        List<OhlcDataDTO> ohlcDataDTOList = ohlcService.loadOhlcData(symbolName, interval);
        return ResponseEntity.ok(ohlcDataDTOList);
    }

    @Hidden
    @PostMapping("/admin/aggregate/{type}")
    public ResponseEntity<?> generateAggregatedOhlcData(@PathVariable String type, @RequestBody List<String> symbols) {
        final Member member = MemberContext.getMember();

        Result<String, String> result = null;
        if ("latest".equals(type)) {
            result = ohlcService.generateLatestWeeklyAndMonthly();
        } else if ("all".equals(type)) {
            result = ohlcService.generateAllWeeklyAndMonthly(symbols);
        }

        if (result.isOk()) {
            return ResponseEntity.ok(result.unwrap());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", result.unwrapErr()));
        }
    }
}
