package com.gurula.stockMate.ohlc;

public enum IntervalType {
    ONE_DAY("1d"),
    ONE_WEEK("1w"),
    ONE_MONTH("1m");

    private final String value;

    IntervalType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static IntervalType fromValue(String value) {
        for (IntervalType type : IntervalType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown interval value: " + value);
    }
}
