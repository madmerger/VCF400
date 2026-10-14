package com.vcf400.domain;

/**
 * CL スタブが RTVJOBA CURUSER で取得し RPG に渡す LAUNCH パラメータ (= 展示 ID)。
 * MM2024 は共用端末モードで、展示 ID を来場者が入力できる (B-13)。
 */
public record Launch(String profile) {
    public static final String MM2024 = "MM2024";

    public Launch {
        // 5250 のユーザープロファイル / LAUNCH パラメータは 9A: 超過分は到達し得ないため切り捨てる
        profile = profile == null ? "" : profile.trim().toUpperCase();
        if (profile.length() > 9) {
            profile = profile.substring(0, 9);
        }
    }

    public boolean isShared() { return MM2024.equals(profile); }
}
