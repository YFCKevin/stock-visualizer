package com.gurula.stockMate.ohlc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurula.stockMate.exception.Result;
import com.gurula.stockMate.symbol.Symbol;
import com.gurula.stockMate.symbol.SymbolRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
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
