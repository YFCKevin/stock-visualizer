package com.gurula.stockMate.ohlc;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OhlcRepository extends MongoRepository<OhlcData, String> {
    List<OhlcData> findBySymbolIdAndIntervalOrderByTimestamp(String symbolId, IntervalType interval);
}
