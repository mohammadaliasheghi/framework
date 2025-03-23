package com.m2a.util;

public class CharacterUtil {
    public static String handleSpecialPersianChars(String source) {
        if (source == null)
            return null;
        source = source.replace("ك", "ک");
        source = source.replace("ي", "ی");
        source = source.replaceAll("\\s+", " ");
        source = source.replaceAll("‬", "");
        source = source.replaceAll("‫", "");
        source = source.replaceAll("‏", "");
        return source.trim();
    }
}
