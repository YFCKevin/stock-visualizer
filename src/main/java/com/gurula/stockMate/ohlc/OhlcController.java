package com.gurula.stockMate.ohlc;

import com.gurula.stockMate.member.Member;
import com.gurula.stockMate.member.MemberContext;
import com.gurula.stockMate.symbol.Symbol;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

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
        Map<String, Object> resultMap = ohlcService.loadOhlcData(symbolName, interval);
        final Symbol symbol = (Symbol) resultMap.get("symbol");
        final List<OhlcData> ohlcData = (List<OhlcData>) resultMap.get("ohlcData");
        final List<OhlcDataDTO> ohlcDataDTOList = ohlcData.stream().map(o -> o.toDto(symbol)).toList();
        return ResponseEntity.ok(ohlcDataDTOList);
    }



}
