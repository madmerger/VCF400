package com.vcf400.service;

import com.vcf400.domain.*;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program EXHBMENU. */
public class ExhibitMenuService {
    private final ExhibitRepository exhibits; private final SettingRepository settings;
    public ExhibitMenuService(ExhibitRepository exhibits, SettingRepository settings) { this.exhibits=exhibits; this.settings=settings; }
    public record MenuView(String title,String name,String city,String state,String desc,boolean showVote,boolean showLrn400) {}
    /** RPG: BEGSR GETEXHB; BR-EXHBMENU-01/02. */
    public MenuView load(String profile) { Exhibit e=exhibits.findById(profile).orElse(null); return e==null?new MenuView(null,null,null,null,null,false,false):new MenuView(e.getExhBtitle(),e.getExhBitor(),e.getExhBcity(),e.getExhBstate(),e.getExhBdesc(),true,e.getEnlRn400()!=0); }
    /** RPG: BEGSR DOVOTE/DOLRN400/ADMKIOSK; BR-EXHBMENU-03/04. */
    public String select(String profile,int opt) { return switch(opt) { case 1->"VOTESTUB"; case 2->"LRN400STUB"; case 3->"ADDGBSTUB"; case 4->"READGBSTUB"; case 7->"ADMKIOSK"; default->"MENU"; }; }
    /** RPG: BEGSR GETPSWRD; BR-EXHBMENU-05. */
    public boolean exitKiosk(String pwd) { return settings.findById("PASSWORD").map(s->s.getValue().equals(pwd)).orElse(false); }
}
