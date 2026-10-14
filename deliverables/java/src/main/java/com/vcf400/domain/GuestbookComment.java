package com.vcf400.domain;

/** GUESTBKDB / GUESTBKRCD (D-03). */
public record GuestbookComment(int cmtid, String visible, String exhbid, String guestname, String guestcmt) {
    public boolean isHidden() { return "N".equals(visible); }
}
