package com.vcf400.domain;

/** EXHBDB / EXHBREC (D-01). */
public record Exhibit(int exhbdbid, String exhusrprf, String exhbitor, String exhbcity, String exhbstate,
                      String exhbtitle, String exhbdesc, int eligible, int enlrn400) {
    public boolean isEligible() { return eligible == 1; }
    public boolean isLearnEnabled() { return enlrn400 == 1; }
}
