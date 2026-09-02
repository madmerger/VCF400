package com.vcf400.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "EXHBDB")
public class Exhibit {
    @Column(name="EXHBDBID") private Integer exhDbId;
    @Id @Column(name="EXHUSRPRF") private String exhUsrPrf;
    @Column(name="EXHBITOR") private String exhBitor;
    @Column(name="EXHBCITY") private String exhBcity;
    @Column(name="EXHBSTATE") private String exhBstate;
    @Column(name="EXHBTITLE") private String exhBtitle;
    @Column(name="EXHBDESC") private String exhBdesc;
    @Column(name="ELIGIBLE") private Integer eligible;
    @Column(name="ENLRN400") private Integer enlRn400;
    protected Exhibit() {}
    public Exhibit(Integer id, String profile, String owner, String city, String state, String title, String desc, Integer eligible, Integer learn) {
        exhDbId=id; exhUsrPrf=profile; exhBitor=owner; exhBcity=city; exhBstate=state; exhBtitle=title; exhBdesc=desc; this.eligible=eligible; enlRn400=learn;
    }
    public Integer getExhDbId() { return exhDbId; }
    public String getExhUsrPrf() { return exhUsrPrf; }
    public String getExhBitor() { return exhBitor; }
    public String getExhBcity() { return exhBcity; }
    public String getExhBstate() { return exhBstate; }
    public String getExhBtitle() { return exhBtitle; }
    public String getExhBdesc() { return exhBdesc; }
    public Integer getEligible() { return eligible; }
    public Integer getEnlRn400() { return enlRn400; }
    public void update(String owner, String city, String state, String title, String desc, Integer eligible, Integer learn) {
        exhBitor=owner; exhBcity=city; exhBstate=state; exhBtitle=title; exhBdesc=desc; this.eligible=eligible; enlRn400=learn;
    }
}
