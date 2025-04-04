package com.m2a.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

@Getter
@Setter
public class TempModel {
    private Long longValue;
    private String stringValue;
    private Date dateValue;
    private Integer integerValue;
    private Float floatValue;
    private Double doubleValue;
    private Boolean boolValue;
    private Byte byteValue;
    private Short shortValue;
    private Object objectValue;
    private Character characterValue;
    private BigDecimal bigDecimalValue;
    private BigInteger bigIntegerValue;
    private Map<String, Object> mapValue;
}
