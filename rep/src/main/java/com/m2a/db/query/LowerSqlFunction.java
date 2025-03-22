package com.m2a.db.query;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LowerSqlFunction implements Function {

    @Override
    public String parseColumn(String columnName) {
        return "lower(" + columnName + ")";
    }

    @Override
    public Object parseColumnValue(Object columnValue) {
        return columnValue != null ?
                String.valueOf(columnValue).toLowerCase() : "";
    }
}
