package com.m2a.util;


public class StringUtil {

    public static boolean isNotEmpty(String input) {
        return (input != null && !input.trim().isEmpty());
    }

    public static boolean isEmpty(String input) {
        if (input == null)
            return true;
        return input.trim().isEmpty();
    }

    public static String cut(String input, int length) {
        if (isEmpty(input))
            return null;
        if (input.length() > length)
            return input.substring(0, length).concat(" ...");
        return input;
    }

    public static String stringCutter(String input, int howMany) {
        return ((input != null && input.length() > howMany) ? input.substring(0, howMany) : input);
    }

    public static int countDuplicateWords(String input1, String input2) {
        int occurence = 0;
        for (int i = 0; i < input1.length(); i++) {
            char at1 = input1.charAt(i);
            for (int j = 0; j < input2.length(); j++) {
                char at2 = input2.charAt(j);
                if (at1 == at2) {
                    if (i == j)
                        occurence++;
                }
            }
        }
        return occurence;
    }

    public static boolean isNotNull(Object input) {
        String val = input == null ? null : String.valueOf(input);
        return isNotEmpty(val);
    }

    public static String stringSpaceRemover(String str) {
        // Remove all space from str
        str = str.replace(" ", "");
        str = str.replace("آ", "ا");
        return CharacterUtil.handleSpecialPersianChars(str);
    }

    public static Boolean stringContains(String sourceStr, String targetStr) {
        if (sourceStr == null || targetStr == null)
            return false;
        if (isEmpty(targetStr) && isNotEmpty(sourceStr))
            return false;
        if (isEmpty(sourceStr) && isNotEmpty(targetStr))
            return false;
        if (stringSpaceRemover(sourceStr.toLowerCase())
                .contains(stringSpaceRemover(targetStr.toLowerCase())))
            return true;
        return stringSpaceRemover(targetStr.toLowerCase())
                .contains(stringSpaceRemover(sourceStr.toLowerCase()));
    }

    public static String removeNewLine(String input) {
        if (isEmpty(input))
            return input;
        return input.replaceAll("\\n", "");
    }

    public static String padLeft(String input, int n, char replace) {
        if (input == null)
            input = "";
        return String.format("%1$" + n + "s", input).replace(' ', replace);
    }

    public static String padRight(String input, int n, char replace) {
        if (input == null) {
            input = "";
        }
        return String.format("%1$-" + n + "s", input).replace(' ', replace);
    }
}
