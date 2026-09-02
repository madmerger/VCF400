package com.vcf400.service;

public final class DdsField {

    private DdsField() {
    }

    public static String truncate(String value, int length) {
        if (value == null || value.length() <= length) {
            return value;
        }
        return value.substring(0, length);
    }
}
