package com.vcf400.service;

import com.vcf400.domain.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
/** Java implementation of RPG program ADMCRTEXHB. */
public class AdminExhibitService {
    private final ExhibitRepository exhibits;
    public AdminExhibitService(ExhibitRepository exhibits) { this.exhibits=exhibits; }
    public record ExhibitForm(String profile,String owner,String city,String state,String title,String description,int eligible,int learn400) {}
    public record ExhibitResult(boolean success,String message,int status,Exhibit exhibit) {}
    /** RPG: BEGSR LOADEXHB; BR-ADMCRTEXHB-01/02. */
    public Exhibit load(String profile) { return exhibits.findById(profile).orElse(null); }
    /** RPG: BEGSR WRITEEXHB; BR-ADMCRTEXHB-03/04/05. */
    public ExhibitResult writeExhb(ExhibitForm f) {
        if(f==null||f.profile()==null||f.profile().isBlank()) return new ExhibitResult(false,RpgMessages.USER_BLANK,5,null);
        boolean exists=exhibits.existsById(f.profile());
        Exhibit e=exhibits.findById(f.profile()).orElse(new Exhibit(0,f.profile(),f.owner(),f.city(),f.state(),f.title(),f.description(),f.eligible(),f.learn400()));
        if(exists) e.update(f.owner(),f.city(),f.state(),f.title(),f.description(),f.eligible(),f.learn400());
        return new ExhibitResult(true,exists?null:RpgMessages.USER_CREATED,0,exhibits.save(e));
    }
    /** RPG: BEGSR DELRCD; BR-ADMCRTEXHB-06. */
    public ExhibitResult delRcd(String profile, boolean confirmed) {
        if(profile==null||profile.isBlank()) return new ExhibitResult(false,RpgMessages.USER_BLANK,5,null);
        Exhibit e=exhibits.findById(profile).orElse(null); if(e==null) return new ExhibitResult(false,RpgMessages.NOT_DELETED,3,null);
        if(!confirmed) return new ExhibitResult(false,"Cancelled",0,e);
        exhibits.delete(e); return new ExhibitResult(true,RpgMessages.DELETED,4,e);
    }
    public List<Exhibit> list() { return exhibits.findAll(); }
}
