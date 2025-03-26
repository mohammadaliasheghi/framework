package com.m2a.web;

import com.m2a.enums.Operator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParameterItem {
    private String propertyName;
    private Operator operator;
    private Object value;

    public ParameterItem(String propertyName, Operator operator, Object value) {
        this.propertyName = propertyName;
        this.operator = operator;
        this.value = value;
    }
}
