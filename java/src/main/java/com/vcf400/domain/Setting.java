package com.vcf400.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "SETTINGS")
public class Setting {
    @Id @Column(name="SETTING") private String setting;
    @Column(name="VALUE") private String value;
    protected Setting() {}
    public Setting(String setting, String value) { this.setting=setting; this.value=value; }
    public String getSetting() { return setting; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value=value; }
}
