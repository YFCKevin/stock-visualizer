package com.gurula.stockMate.ohlc;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OhlcRepository extends MongoRepository<OhlcData, String> {
    List<OhlcData> findBySymbolIdAndIntervalOrderByTimestamp(String symbolId, IntervalType interval);

    List<OhlcData> findBySymbolIdInAndIntervalAndTimestampBetween(List<String> symbolIds, IntervalType intervalType, long startOfSevenDaysAgo, long startOfTomorrow);

    List<OhlcData> findByIdIn(List<String> ohlcIds);

    Optional<OhlcData> findBySymbolIdAndIntervalAndDate(String symbolId, IntervalType intervalType, String date);
}
