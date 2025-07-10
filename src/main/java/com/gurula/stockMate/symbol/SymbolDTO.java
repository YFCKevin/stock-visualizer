package com.gurula.stockMate.symbol;

import com.gurula.stockMate.symbol.SymbolType;
import com.gurula.stockMate.symbol.Symbol;

public class SymbolDTO {
    private String id;
    private String symbol;
    private String name;
    private String market;  // 地區
    private SymbolType symbolType;   // 股票類型
    private String currency;   // 幣別

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(SymbolType symbolType) {
        this.symbolType = symbolType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Symbol toEntity() {
        Symbol symbol = new Symbol();
        symbol.setCurrency(this.currency);
        symbol.setSymbolType(this.symbolType);
        symbol.setName(this.name);
        symbol.setMarket(this.market);
        symbol.setSymbol(this.symbol);
        return symbol;
    }
}
