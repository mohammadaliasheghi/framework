package com.m2a.util;

import java.util.Collection;

public class CollectionUtil {

    public static boolean isEmpty(Collection<?> input) {
        return input == null || input.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> input) {
        return input != null && !input.isEmpty();
    }
}
