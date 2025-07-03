package com.gurula.stockMate.ohlc;

import com.gurula.stockMate.symbol.Symbol;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ohlc_data")
public class OhlcData {
    @Id
    private String id;
    private String symbolId;
    private String date;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
    private long timestamp;
    private IntervalType interval;

    public OhlcDataDTO toDto(Symbol symbol) {
        OhlcDataDTO dto = new OhlcDataDTO();
        dto.setSymbol(symbol.getSymbol());
        dto.setSymbolName(symbol.getName());
        dto.setId(this.id);
        dto.setOpen(this.open);
        dto.setClose(this.close);
        dto.setHigh(this.high);
        dto.setLow(this.low);
        dto.setVolume(this.volume);
        dto.setTimestamp(this.timestamp);
        dto.setInterval(this.interval);
        return dto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSymbolId() {
        return symbolId;
    }

    public void setSymbolId(String symbolId) {
        this.symbolId = symbolId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public IntervalType getInterval() {
        return interval;
    }

    public void setInterval(IntervalType interval) {
        this.interval = interval;
    }
}
