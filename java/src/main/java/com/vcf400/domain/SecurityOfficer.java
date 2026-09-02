package com.vcf400.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "SECOFRS")
public class SecurityOfficer {

    @Id
    @Column(name = "USERPROF")
    private String userProf;

    protected SecurityOfficer() {
    }

    public SecurityOfficer(String profile) {
        userProf = profile;
    }

    public String getUserProf() {
        return userProf;
    }
}
