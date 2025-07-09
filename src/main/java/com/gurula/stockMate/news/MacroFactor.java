package com.gurula.stockMate.news;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum MacroFactor {
    OIL_PRICE("油價", "Oil Price"),
    INFLATION("通膨", "Inflation"),
    INTEREST_RATE("利率", "Interest Rate"),
    ECONOMY("經濟", "Economy"),
    STOCK_MARKET("股市", "Stock Market"),
    BOND_MARKET("債市", "Bond Market"),
    FOREIGN_EXCHANGE("匯率", "Foreign Exchange"),
    HOUSING_MARKET("房市", "Housing Market"),
    CURRENT_EVENTS("時事", "Current Events");

    private final String zhLabel;
    private final String enLabel;

    MacroFactor(String zhLabel, String enLabel) {
        this.zhLabel = zhLabel;
        this.enLabel = enLabel;
    }

    public String getZhLabel() {
        return zhLabel;
    }

    public String getEnLabel() {
        return enLabel;
    }
}