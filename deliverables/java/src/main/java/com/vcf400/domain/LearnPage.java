package com.vcf400.domain;

/** LRN400STR / LRN400RCD (D-07). */
public record LearnPage(int pagenbr, String content, String extra) {
    public boolean isEnd() { return "END".equals(extra); }
}
