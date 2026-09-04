package com.vcf400.domain;

import java.io.Serializable;
import java.util.Objects;

public class Lrn400PageId implements Serializable {

    private String owner;
    private Integer pageNbr;

    public Lrn400PageId() {
    }

    public Lrn400PageId(String owner, Integer pageNbr) {
        this.owner = owner;
        this.pageNbr = pageNbr;
    }

    public String getOwner() {
        return owner;
    }

    public Integer getPageNbr() {
        return pageNbr;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Lrn400PageId other)) {
            return false;
        }
        return Objects.equals(owner, other.owner)
                && Objects.equals(pageNbr, other.pageNbr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, pageNbr);
    }
}
