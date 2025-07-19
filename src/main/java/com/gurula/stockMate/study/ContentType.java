package com.gurula.stockMate.study;

import com.gurula.stockMate.layout.Layout;
import com.gurula.stockMate.news.News;
import com.gurula.stockMate.note.Note;

import java.util.Arrays;

public enum ContentType {
    LAYOUT("layout", Layout.class),
    NOTE("note", Note.class),
    NEWS("news", News.class);

    private final String type;
    private final Class<?> entityClass;

    private ContentType(String type, Class entityClass) {
        this.type = type;
        this.entityClass = entityClass;
    }

    public String getType() {
        return this.type;
    }

    public Class<?> getEntityClass() {
        return this.entityClass;
    }

    public static ContentType fromType(String type) {
        return (ContentType) Arrays.stream(values()).filter((ct) -> {
            return ct.type.equalsIgnoreCase(type);
        }).findFirst().orElseThrow(() -> {
            return new IllegalArgumentException("Unknown content type: " + type);
        });
    }
}

