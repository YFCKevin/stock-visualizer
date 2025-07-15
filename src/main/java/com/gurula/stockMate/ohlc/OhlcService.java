package com.gurula.stockMate.ohlc;

import com.gurula.stockMate.exception.Result;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

public interface OhlcService {
    Map<String, Object> loadOhlcData(String symbolName, String interval);

    Result<String, String> generateLatestWeeklyAndMonthly();
}
