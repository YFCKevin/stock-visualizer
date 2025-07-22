package com.gurula.stockMate.ohlc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.symbol.Symbol;
import com.gurula.stockMate.symbol.SymbolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class OhlcServiceImpl implements OhlcService{
    private final OhlcRepository ohlcRepository;
    private final SymbolRepository symbolRepository;

    public OhlcServiceImpl(OhlcRepository ohlcRepository, SymbolRepository symbolRepository) {
        this.ohlcRepository = ohlcRepository;
        this.symbolRepository = symbolRepository;
    }

    @Override
    public Map<String, Object> loadOhlcData(String symbolName, String interval) {
        final Symbol symbol = symbolRepository.findBySymbol(symbolName).get();
        final IntervalType intervalType = IntervalType.fromValue(interval);
        final List<OhlcData> ohlcData = ohlcRepository.findBySymbolIdAndIntervalOrderByTimestamp(symbol.getId(), intervalType).stream().toList();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("symbol", symbol);
        resultMap.put("ohlcData", ohlcData);
        return resultMap;
    }

    @Override
    public Result<String, String> generateLatestWeeklyAndMonthly() {
        final List<String> symbolIds = symbolRepository.findAll().stream()
                .map(Symbol::getId)
                .toList();

        ZoneId zoneId = ZoneId.of("Asia/Taipei");
        LocalDate now = LocalDate.now(zoneId);
        LocalDate weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate monthStart = now.withDayOfMonth(1);

        for (String symbolId : symbolIds) {
            try {
                List<OhlcData> dailyData = ohlcRepository.findBySymbolIdAndIntervalOrderByTimestamp(
                        symbolId, IntervalType.ONE_DAY);

                if (dailyData == null || dailyData.isEmpty()) {
                    continue;
                }

                System.out.println("==========");
                System.out.println("Symbol ID: " + symbolId);
                System.out.println("週線資料範圍: " + weekStart + " ~ " + weekStart.plusDays(6));
                System.out.println("月線資料範圍: " + monthStart + " ~ " + monthStart.withDayOfMonth(monthStart.lengthOfMonth()));

                Optional<OhlcData> existingWeekly = ohlcRepository.findBySymbolIdAndIntervalAndDate(
                        symbolId, IntervalType.ONE_WEEK, weekStart.toString());

                Optional<OhlcData> existingMonthly = ohlcRepository.findBySymbolIdAndIntervalAndDate(
                        symbolId, IntervalType.ONE_MONTH, monthStart.toString());

                List<OhlcData> weekData = dailyData.stream()
                        .filter(d -> {
                            try {
                                LocalDate dDate = LocalDate.parse(d.getDate());
                                return !dDate.isBefore(weekStart) && !dDate.isAfter(weekStart.plusDays(6));
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .toList();

                List<OhlcData> monthData = dailyData.stream()
                        .filter(d -> {
                            try {
                                LocalDate dDate = LocalDate.parse(d.getDate());
                                return dDate.getYear() == monthStart.getYear() && dDate.getMonth() == monthStart.getMonth();
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .toList();

                if (!weekData.isEmpty()) {
                    OhlcData weekly = buildAggregatedOhlc(weekData, symbolId, IntervalType.ONE_WEEK, weekStart, zoneId);
                    existingWeekly.ifPresentOrElse(
                            existing -> {
                                weekly.setId(existing.getId());
                                ohlcRepository.save(weekly);
                            },
                            () -> ohlcRepository.save(weekly)
                    );
                }

                if (!monthData.isEmpty()) {
                    OhlcData monthly = buildAggregatedOhlc(monthData, symbolId, IntervalType.ONE_MONTH, monthStart, zoneId);
                    existingMonthly.ifPresentOrElse(
                            existing -> {
                                monthly.setId(existing.getId());
                                ohlcRepository.save(monthly);
                            },
                            () -> ohlcRepository.save(monthly)
                    );
                }

            } catch (Exception e) {
                return Result.err("處理 symbolId " + symbolId + " 發生例外: " + e.getMessage());
            }
        }

        return Result.ok("週線/月線資料已更新");
    }


    @Override
    @Transactional
    public Result<String, String> generateAllWeeklyAndMonthly(List<String> symbols) {
        ZoneId zoneId = ZoneId.of("Asia/Taipei");

        for (String symbol : symbols) {
            Optional<Symbol> opt = symbolRepository.findBySymbol(symbol);
            if (opt.isEmpty()) {
                System.out.println("找不到 Symbol: " + symbol);
                continue;
            }

            String symbolId = opt.get().getId();
            List<OhlcData> dailyData = ohlcRepository.findBySymbolIdAndIntervalOrderByTimestamp(
                    symbolId, IntervalType.ONE_DAY);

            if (dailyData == null || dailyData.isEmpty()) {
                System.out.println("Symbol " + symbol + " 無日線資料");
                continue;
            }

            // 聚合週線資料
            Map<LocalDate, List<OhlcData>> weeklyGroups = new HashMap<>();
            Map<YearMonth, List<OhlcData>> monthlyGroups = new HashMap<>();

            for (OhlcData data : dailyData) {
                try {
                    LocalDate date = LocalDate.parse(data.getDate());
                    LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    YearMonth ym = YearMonth.from(date);

                    weeklyGroups.computeIfAbsent(weekStart, k -> new ArrayList<>()).add(data);
                    monthlyGroups.computeIfAbsent(ym, k -> new ArrayList<>()).add(data);
                } catch (Exception e) {
                    System.out.println("日期解析錯誤: " + data.getDate());
                }
            }

            // 準備要儲存的週線/月線資料
            List<OhlcData> toSave = new ArrayList<>();

            // 處理週線
            for (Map.Entry<LocalDate, List<OhlcData>> entry : weeklyGroups.entrySet()) {
                LocalDate weekStart = entry.getKey();
                List<OhlcData> weekData = entry.getValue();

                OhlcData weekly = buildAggregatedOhlc(weekData, symbolId, IntervalType.ONE_WEEK, weekStart, zoneId);

                ohlcRepository.findBySymbolIdAndIntervalAndDate(symbolId, IntervalType.ONE_WEEK, weekStart.toString())
                        .ifPresent(existing -> weekly.setId(existing.getId()));

                toSave.add(weekly);
            }

            // 處理月線
            for (Map.Entry<YearMonth, List<OhlcData>> entry : monthlyGroups.entrySet()) {
                YearMonth ym = entry.getKey();
                LocalDate monthStart = ym.atDay(1);
                List<OhlcData> monthData = entry.getValue();

                OhlcData monthly = buildAggregatedOhlc(monthData, symbolId, IntervalType.ONE_MONTH, monthStart, zoneId);

                ohlcRepository.findBySymbolIdAndIntervalAndDate(symbolId, IntervalType.ONE_MONTH, monthStart.toString())
                        .ifPresent(existing -> monthly.setId(existing.getId()));

                toSave.add(monthly);
            }

            if (!toSave.isEmpty()) {
                System.out.println(toSave.size());
                ohlcRepository.saveAll(toSave);
                System.out.println("已儲存 Symbol: " + symbol + " 的週線/月線共 " + toSave.size() + " 筆資料");
            }
        }

        return Result.ok("全部週線/月線資料已批量更新完成");
    }


    private OhlcData buildAggregatedOhlc(List<OhlcData> group,
                                         String symbolId,
                                         IntervalType interval,
                                         LocalDate periodStart,
                                         ZoneId zoneId) {
        if (group == null || group.isEmpty()) {
            throw new IllegalArgumentException("Group is empty during aggregation.");
        }

        List<OhlcData> sortedGroup = new ArrayList<>(group);
        sortedGroup.sort(Comparator.comparingLong(OhlcData::getTimestamp));

        OhlcData ohlc = new OhlcData();
        ohlc.setSymbolId(symbolId);
        ohlc.setInterval(interval);
        ohlc.setDate(periodStart.toString());
        ohlc.setTimestamp(periodStart.atStartOfDay(zoneId).toInstant().toEpochMilli());

        ohlc.setOpen(sortedGroup.get(0).getOpen());
        ohlc.setClose(sortedGroup.get(sortedGroup.size() - 1).getClose());
        ohlc.setHigh(sortedGroup.stream().mapToDouble(d -> d.getHigh() != 0 ? d.getHigh() : 0).max().orElse(0));
        ohlc.setLow(sortedGroup.stream().mapToDouble(d -> d.getLow() != 0 ? d.getLow() : 0).min().orElse(0));
        ohlc.setVolume(sortedGroup.stream().mapToDouble(d -> d.getVolume() != 0 ? d.getVolume() : 0).sum());

        return ohlc;
    }
}
