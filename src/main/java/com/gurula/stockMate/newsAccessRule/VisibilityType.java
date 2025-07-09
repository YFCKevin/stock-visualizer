package com.gurula.stockMate.newsAccessRule;

public enum VisibilityType {
    PUBLIC("public", "所有人可見"),
    PRIVATE("private", "僅本人可見"),
    RESTRICTED("restricted", "指定人可見"),
    GROUP("group", "特定群組可見"),
    ROLE("role", "特定角色可見");

    private final String value;
    private final String label;

    VisibilityType(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public static VisibilityType fromValue(String value) {
        for (VisibilityType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown visibility type: " + value);
    }
}
