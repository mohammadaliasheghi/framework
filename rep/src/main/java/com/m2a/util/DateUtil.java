package com.m2a.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class DateUtil {

    public static Date toDate(Object value) {
        if (value instanceof Date)
            return (Date) value;
        else if (value instanceof Long)
            return new Date((Long) value);
        throw new IllegalArgumentException("cannot cast " + value + " to java.util.Date");
    }

    public static LocalDate convertToLocalDate(Date dateToConvert, ZoneId zoneId) {
        return dateToConvert.toInstant()
                .atZone(zoneId)
                .toLocalDate();
    }

    public static LocalDate convertToLocalDate(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}