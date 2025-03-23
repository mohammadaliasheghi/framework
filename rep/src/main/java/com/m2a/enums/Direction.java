package com.m2a.enums;

import lombok.Getter;

@Getter
public enum Direction {
    ASC("asc"),
    DESC("desc"),
    NULLS_FIRST("NULLS FIRST"),
    NULLS_LAST("NULLS LAST"),
    QUERY("query"),
    DESC_NULLS_LAST("DESC NULLS LAST"),
    DESC_NULLS_FIRST("DESC NULLS FIRST");

    private final String label;

    Direction(String label) {
        this.label = label;
    }
}