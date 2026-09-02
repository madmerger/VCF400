package com.vcf400.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "LRN400STR")
@IdClass(Lrn400PageId.class)
public class Lrn400Page {
    @Id @Column(name="OWNER") private String owner;
    @Id @Column(name="PAGENBR") private Integer pageNbr;
    @Column(name="CONTENT") private String content;
    @Column(name="EXTRA") private String extra;
    protected Lrn400Page() {}
    public Lrn400Page(String owner, Integer page, String content, String extra) { this.owner=owner; pageNbr=page; this.content=content; this.extra=extra; }
    public String getOwner() { return owner; }
    public Integer getPageNbr() { return pageNbr; }
    public String getContent() { return content; }
    public String getExtra() { return extra; }
    public void update(String content, String extra) { this.content=content; this.extra=extra; }
}
