package com.m2a.db.query;

public interface Function {

    String parseColumn(String columnName);

    Object parseColumnValue(Object columnValue);
}
