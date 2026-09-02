package com.vcf400.domain;

import java.io.Serializable;

public class Lrn400PageId implements Serializable {
    private String owner;
    private Integer pageNbr;
    public Lrn400PageId() {}
    public Lrn400PageId(String owner, Integer pageNbr) { this.owner=owner; this.pageNbr=pageNbr; }
    public String getOwner() { return owner; }
    public Integer getPageNbr() { return pageNbr; }
    @Override public boolean equals(Object o) { return o instanceof Lrn400PageId i && owner.equals(i.owner) && pageNbr.equals(i.pageNbr); }
    @Override public int hashCode() { return 31 * owner.hashCode() + pageNbr.hashCode(); }
}
