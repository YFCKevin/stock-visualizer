package com.gurula.stockMate.ohlc;

import com.gurula.stockMate.exception.Result;

import java.util.List;
import java.util.Map;

public interface OhlcService {
    Map<String, Object> loadOhlcData(String symbolName, String interval);

    Result<String, String> generateLatestWeeklyAndMonthly();

    Result<String, String> generateAllWeeklyAndMonthly(List<String> symbols);
}
