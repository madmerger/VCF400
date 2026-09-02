package com.vcf400.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "GUESTBKDB")
public class GuestbookComment {
    @Id @Column(name="CMTID") private Integer cmtId;
    @Column(name="VISIBLE") private String visible;
    @Column(name="EXHBID") private String exhbId;
    @Column(name="GUESTNAME") private String guestName;
    @Column(name="GUESTCMT") private String guestCmt;
    protected GuestbookComment() {}
    public GuestbookComment(Integer id, String visible, String exhibit, String name, String comment) { cmtId=id; this.visible=visible; exhbId=exhibit; guestName=name; guestCmt=comment; }
    public Integer getCmtId() { return cmtId; }
    public String getVisible() { return visible; }
    public String getExhbId() { return exhbId; }
    public String getGuestName() { return guestName; }
    public String getGuestCmt() { return guestCmt; }
    public void setVisible(String value) { visible=value; }
}
