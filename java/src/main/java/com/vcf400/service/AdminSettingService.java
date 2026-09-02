package com.vcf400.service;

import com.vcf400.domain.*;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.stream.Collectors;

@Service
/** Java implementation of RPG program ADMSETTING. */
public class AdminSettingService {
    private final SettingRepository settings;
    public AdminSettingService(SettingRepository settings) { this.settings=settings; }
    /** RPG: initial SETTINGS READ; BR-ADMSETTING-01. */
    public Map<String,String> load() { return settings.findAll().stream().collect(Collectors.toMap(Setting::getSetting,Setting::getValue)); }
    /** RPG: SETUPMAIN/UPDATE; BR-ADMSETTING-02/03. */
    public Map<String,String> save(String password,String allowVote) {
        settings.findById("PASSWORD").ifPresent(s->{s.setValue(password);settings.save(s);});
        settings.findById("ALWVOTE").ifPresent(s->{s.setValue(allowVote);settings.save(s);}); return load();
    }
}
