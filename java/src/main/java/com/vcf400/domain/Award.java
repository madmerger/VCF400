package com.vcf400.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "AWARDDB")
public class Award {
    @Id
    @Column(name="AWARDID")
    private Integer awardId;
    @Column(name="AWARDTITLE")
    private String awardTitle;
    @Column(name="AWARDDESC")
    private String awardDesc;
    protected Award() {}
    public Award(Integer id, String title, String desc) { awardId = id; awardTitle = title; awardDesc = desc; }
    public Integer getAwardId() { return awardId; }
    public String getAwardTitle() { return awardTitle; }
    public String getAwardDesc() { return awardDesc; }
}
