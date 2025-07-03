package com.gurula.stockMate.ohlc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurula.stockMate.symbol.Symbol;
import com.gurula.stockMate.symbol.SymbolRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
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


}
