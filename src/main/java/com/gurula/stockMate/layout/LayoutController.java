package com.gurula.stockMate.layout;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.layout.dto.LayoutDTO;
import com.gurula.stockMate.layout.dto.LayoutSummaryDTO;
import com.gurula.stockMate.member.Member;
import com.gurula.stockMate.member.MemberContext;
import com.gurula.stockMate.ohlc.OhlcData;
import com.gurula.stockMate.ohlc.OhlcDataDTO;
import com.gurula.stockMate.ohlc.OhlcService;
import com.gurula.stockMate.symbol.Symbol;
import com.gurula.stockMate.symbol.SymbolService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/layout")
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
    public ResponseEntity<?> getAllLayouts() {
        final Member member = MemberContext.getMember();
        final List<LayoutSummaryDTO> allLayouts = layoutService.getAllLayouts(member.getId());
        return ResponseEntity.ok(allLayouts);
    }


    @GetMapping("/search")
    public ResponseEntity<?> searchLayouts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String symbolName
    ) {
        final Member member = MemberContext.getMember();
        List<LayoutSummaryDTO> layouts = layoutService.search(member.getId(), name, symbol, symbolName);
        return ResponseEntity.ok(layouts);
    }

    @GetMapping("/enter")
    public ResponseEntity<?> enterLayout(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String symbolName
    ) {
        System.out.println("id = " + id);
        System.out.println("symbolName = " + symbolName);
        String interval = "1d";
        final Member member = MemberContext.getMember();
        Result<Layout, String> result = null;
        if (StringUtils.isNotBlank(id)) {
            // 有傳 id，直接根據 id查找 layout，並且用 memberId驗證是否為該會員
            result = layoutService.findByIdAndMemberId(id, member.getId());

            if (result.isErr()) {
                String errorMessage = result.unwrapErr();
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
        Map<String, Object> resultMap = ohlcService.loadOhlcData(symbolName, interval);
        final Symbol symbol = (Symbol) resultMap.get("symbol");
        final List<OhlcData> ohlcData = (List<OhlcData>) resultMap.get("ohlcData");
        final List<OhlcDataDTO> ohlcDataDTOList = ohlcData.stream().map(o -> o.toDto(symbol)).toList();

        if (result.isOk()) {
            Layout layout = result.unwrap();
            final LayoutDTO dto = layout.toDto();
            dto.setOhlcDataDTOList(ohlcDataDTOList);
            dto.setSymbol(symbolName);
            return ResponseEntity.ok(dto);
        } else {
            String errorMessage = result.unwrapErr();

            if (errorMessage.equals("找不到對應的 Layout 資料")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", errorMessage));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }
    }


    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody LayoutDTO layoutDTO) {
        final Member member = MemberContext.getMember();
        layoutDTO.setMemberId(member.getId());
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
    }


    @PatchMapping("/edit")
    public ResponseEntity<?> edit(@RequestBody LayoutDTO layoutDTO) {
        final Member member = MemberContext.getMember();
        layoutDTO.setMemberId(member.getId());
        Result<Layout, String> result = layoutService.edit(layoutDTO);
        if (result.isOk()) {
            Layout layout = result.unwrap();
            LayoutSummaryDTO dto = LayoutSummaryDTO.construct(layout);
            return ResponseEntity.ok(dto);
        } else {
            String errorMessage = result.unwrapErr();

            if (errorMessage.equals("找不到對應的 Layout 資料")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", errorMessage));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") String id) {
        final Member member = MemberContext.getMember();
        Result<String, String> result = layoutService.delete(id);
        if (result.isOk()) {
            return ResponseEntity.ok(result.unwrap());
        } else {
            String errorMessage = result.unwrapErr();

            if (errorMessage.equals("找不到對應的 Layout 資料")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", errorMessage));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", errorMessage));
        }
    }
}
