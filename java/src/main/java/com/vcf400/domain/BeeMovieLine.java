package com.vcf400.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "BMOVDB")
public class BeeMovieLine {

    @Column(name = "SPEAKER")
    private String speaker;

    @Id
    @Column(name = "LINE")
    private String line;

    protected BeeMovieLine() {
    }

    public BeeMovieLine(String speaker, String line) {
        this.speaker = speaker;
        this.line = line;
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getLine() {
        return line;
    }
}
