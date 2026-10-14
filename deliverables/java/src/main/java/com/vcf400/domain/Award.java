package com.vcf400.domain;

/** AWARDDB / AWARDRCD (D-04). */
public record Award(int awardid, String awardtitle, String awarddesc) {
    /** 元画面の "001. Best in Show Award" 表記に合わせた 3 桁ゼロ埋め番号。 */
    public String number() { return String.format("%03d", awardid); }
}
