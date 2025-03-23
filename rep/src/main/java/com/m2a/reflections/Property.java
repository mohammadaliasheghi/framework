package com.m2a.reflections;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@RequiredArgsConstructor
public class Property implements Serializable {
    private String columnName;
    private String fieldName;

    public Property(String columnName, String fieldName) {
        this.columnName = columnName;
        this.fieldName = fieldName;
    }
}
