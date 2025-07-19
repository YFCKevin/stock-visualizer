package com.gurula.stockMate.symbol;

public class SymbolDataDTO {
    private String date;
    private String symbol;
    private String name;
    private double close;
    private double change;
    private double changePercent;
    private SymbolType symbolType;
    private String symbolTypeLabel;
    private double volume;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(SymbolType symbolType) {
        this.symbolType = symbolType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }

    public String getSymbolTypeLabel() {
        return symbolTypeLabel;
    }

    public void setSymbolTypeLabel(String symbolTypeLabel) {
        this.symbolTypeLabel = symbolTypeLabel;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }
}
