package com.gurula.stockMate.cache;

import com.gurula.stockMate.ohlc.IntervalType;
import com.gurula.stockMate.ohlc.OhlcData;
import com.gurula.stockMate.ohlc.OhlcDataDTO;
import com.gurula.stockMate.ohlc.OhlcRepository;
import com.gurula.stockMate.symbol.Symbol;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CacheService {
    private final OhlcRepository ohlcRepository;

    public CacheService(OhlcRepository ohlcRepository) {
        this.ohlcRepository = ohlcRepository;
    }

    @Cacheable(
            value = "ohlcCache",
            key = "#symbol.id + '_' + #interval",
            unless = "#result.size() == 0"  // 沒有取得資料不執行快取
    )
    public List<OhlcDataDTO> getOhlcData(Symbol symbol, IntervalType interval) {
        System.out.println("[cache] " + symbol.getId() + " / " + interval);
        final List<OhlcData> ohlcDataList = ohlcRepository.findBySymbolIdAndIntervalOrderByTimestamp(symbol.getId(), interval);
        return ohlcDataList.stream().map(ohlcData -> ohlcData.toDto(symbol)).toList();
    }

    @CacheEvict(value = "ohlcCache", key = "#symbolId + '_' + #interval")
    public void evictCache(String symbolId, IntervalType interval) {
        System.out.println("[evictCache] " + symbolId + " / " + interval);
    }
}
