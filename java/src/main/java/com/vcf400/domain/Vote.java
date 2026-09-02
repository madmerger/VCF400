package com.vcf400.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "VOTINGDB")
public class Vote {
    @Id
    @Column(name="BADGENBR")
    private Integer badgeNbr;
    @Column(name="AWARDNBR")
    private Integer awardNbr;
    @Column(name="EXHBNBR")
    private String exhNbr;
    protected Vote() {}
    public Vote(Integer badgeNbr, Integer awardNbr, String exhNbr) { this.badgeNbr = badgeNbr; this.awardNbr = awardNbr; this.exhNbr = exhNbr; }
    public Integer getBadgeNbr() { return badgeNbr; }
    public Integer getAwardNbr() { return awardNbr; }
    public String getExhNbr() { return exhNbr; }
}
