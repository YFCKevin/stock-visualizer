package com.gurula.stockMate.symbol;

public enum SymbolType {
    STOCK("個股"),
    INDEX("指數"),
    ETF("ETF"),
    FUTURE("期貨"),
    OPTION("選擇權"),
    CRYPTO("加密貨幣"),
    FOREX("外匯"),
    COMMODITY("大宗商品"),
    BOND_YIELD("債券殖利率");

    private final String name;

    SymbolType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
