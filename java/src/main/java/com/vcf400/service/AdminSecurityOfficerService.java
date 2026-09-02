package com.vcf400.service;

import com.vcf400.domain.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
/** Java implementation of RPG programs ADMADDSOFR and ADMOFRLIST. */
public class AdminSecurityOfficerService {
    private final SecurityOfficerRepository officers;
    public AdminSecurityOfficerService(SecurityOfficerRepository officers) { this.officers=officers; }
    /** RPG: BEGSR ADDTODB; BR-ADMADDSOFR-01. */
    public String add(String profile) {
        if(profile==null||profile.isBlank()) return RpgMessages.USER_BLANK;
        if(officers.existsById(profile)) return RpgMessages.USER_EXISTS;
        officers.save(new SecurityOfficer(profile)); return RpgMessages.STATUS_UPDATED;
    }
    /** RPG: ADMOFRLIST copied add logic; BR-ADMADDSOFR-02. */
    public List<SecurityOfficer> list() { return officers.findAll(); }
}
