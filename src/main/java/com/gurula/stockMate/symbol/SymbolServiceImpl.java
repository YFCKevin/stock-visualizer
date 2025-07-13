package com.gurula.stockMate.symbol;

import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.ohlc.IntervalType;
import com.gurula.stockMate.ohlc.OhlcData;
import com.gurula.stockMate.ohlc.OhlcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SymbolServiceImpl implements SymbolService{
    private final SymbolRepository symbolRepository;
    private final OhlcRepository ohlcRepository;

    public SymbolServiceImpl(SymbolRepository symbolRepository, OhlcRepository ohlcRepository) {
        this.symbolRepository = symbolRepository;
        this.ohlcRepository = ohlcRepository;
    }

    @Override
    public List<SymbolDataDTO> getAllSymbols() {
        List<Symbol> symbols = symbolRepository.findAll();
        List<String> symbolIds = symbols.stream().map(Symbol::getId).toList();

        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(7);

        long startOfSevenDaysAgo = sevenDaysAgo.atStartOfDay(zone).toInstant().toEpochMilli();
        long startOfTomorrow = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli();

        // 查最近7天的OHLC日線資料
        List<OhlcData> recentData = ohlcRepository.findBySymbolIdInAndIntervalAndTimestampBetween(
                symbolIds, IntervalType.ONE_DAY, startOfSevenDaysAgo, startOfTomorrow);

        // 按 symbolId 分組
        Map<String, List<OhlcData>> groupedBySymbol = recentData.stream()
                .collect(Collectors.groupingBy(OhlcData::getSymbolId));

        List<SymbolDataDTO> result = new ArrayList<>();
        for (Symbol symbol : symbols) {
            String symbolId = symbol.getId();
            List<OhlcData> dataList = groupedBySymbol.get(symbolId);

            if (dataList == null || dataList.size() < 2) {
                // 沒有足夠資料，跳過
                continue;
            }

            // timestamp倒序排序 (最新在前)
            dataList.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

            OhlcData latest = dataList.get(0);

            BigDecimal todayClose = BigDecimal.valueOf(latest.getClose())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal prevClose = BigDecimal.valueOf(dataList.get(1).getClose());

            BigDecimal change = todayClose.subtract(prevClose).setScale(2, RoundingMode.HALF_UP);
            BigDecimal changePercent = change.divide(prevClose, 6, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);

            SymbolDataDTO dto = new SymbolDataDTO();
            dto.setDate(latest.getDate());
            dto.setName(symbol.getName());
            dto.setClose(todayClose.doubleValue());
            dto.setChange(change.doubleValue());
            dto.setChangePercent(changePercent.doubleValue());
            dto.setSymbolType(symbol.getSymbolType());
            dto.setSymbol(symbol.getSymbol());
            dto.setSymbolTypeLabel(symbol.getSymbolType().getName());

            result.add(dto);
        }

        return result;
    }

    @Override
    @Transactional
    public Result<String, String> delete(String id) {
        return symbolRepository.findById(id)
                .map(symbol -> {
                    symbolRepository.deleteById(id);
                    return Result.<String, String>ok("ok");
                })
                .orElseGet(() -> Result.err("找不到對應的 Symbol 資料"));
    }

    @Override
    @Transactional
    public Result<String, String> saveAll(List<SymbolDTO> symbolDTOList) {
        try {
            final List<Symbol> symbols = symbolDTOList.stream().map(SymbolDTO::toEntity).toList();
            symbolRepository.saveAll(symbols);
            return Result.ok("ok");
        } catch (Exception e) {
            return Result.err("儲存失敗：" + e.getMessage());
        }
    }

    @Override
    public Optional<Symbol> findById(String symbolId) {
        return symbolRepository.findById(symbolId);
    }
}
