package com.gurula.stockMate.symbol;

import com.gurula.stockMate.exception.Result;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Symbol API", description = "股市總覽")
@RestController
@RequestMapping("/symbol")
public class SymbolController {
    private final SymbolService symbolService;

    public SymbolController(SymbolService symbolService) {
        this.symbolService = symbolService;
    }

    @Operation(summary = "取得全球股市列表")
    @GetMapping
    public ResponseEntity<?> getAllSymbols() {
        List<SymbolDataDTO> symbolData = symbolService.getAllSymbols();
        return ResponseEntity.ok(symbolData);
    }

    @Hidden
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody List<SymbolDTO> symbolDTOList) {
        Result<String, String> result = symbolService.saveAll(symbolDTOList);
        if (result.isOk()) {
            return ResponseEntity.ok(result.unwrap());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", result.unwrapErr()));
        }
    }

    @Hidden
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") String id) {
        Result<String, String> result = symbolService.delete(id);
        if (result.isOk()) {
            return ResponseEntity.ok(result.unwrap());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", result.unwrapErr()));
        }
    }

    @Operation(summary = "根據股票代號、名稱、市場別做搜尋股市")
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String name) {
        List<Symbol> symbols = symbolService.search(name);
        return ResponseEntity.ok(symbols);
    }
}
