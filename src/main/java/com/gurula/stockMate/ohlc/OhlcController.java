package com.gurula.stockMate.ohlc;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.member.Member;
import com.gurula.stockMate.member.MemberContext;
import com.gurula.stockMate.symbol.Symbol;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/ohlc")
public class OhlcController {
    private final OhlcService ohlcService;

    public OhlcController(OhlcService ohlcService) {
        this.ohlcService = ohlcService;
    }

    @GetMapping("/{symbolName}/{interval}")
    public ResponseEntity<?> loadOhlcData(
            @PathVariable(name = "symbolName") String symbolName,
            @PathVariable(name = "interval") String interval
    ) {
        final Member member = MemberContext.getMember();
        List<OhlcDataDTO> ohlcDataDTOList = ohlcService.loadOhlcData(symbolName, interval);
        return ResponseEntity.ok(ohlcDataDTOList);
    }


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
