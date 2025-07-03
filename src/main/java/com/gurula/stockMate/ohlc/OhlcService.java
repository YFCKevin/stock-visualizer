package com.gurula.stockMate.ohlc;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

public interface OhlcService {
    Map<String, Object> loadOhlcData(String symbolName, String interval);

}
