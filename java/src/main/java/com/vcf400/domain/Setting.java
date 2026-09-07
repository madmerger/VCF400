package com.vcf400.domain;

/** SETTINGS / SETTINGSR (D-05). */
public record Setting(String setting, String value) {
    public static final String ADMPSWRD = "ADMPSWRD";
    public static final String ALWVOTE = "ALWVOTE";
}
