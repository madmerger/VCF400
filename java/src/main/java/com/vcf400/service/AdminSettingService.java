package com.vcf400.service;

import java.util.Map;
import java.util.stream.Collectors;

import com.vcf400.domain.Setting;
import com.vcf400.domain.SettingRepository;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program ADMSETTING. */
public class AdminSettingService {

    private final SettingRepository settings;

    public AdminSettingService(SettingRepository settings) {
        this.settings = settings;
    }

    /** RPG: initial SETTINGS READ; BR-ADMSETTING-01. */
    public Map<String, String> load() {
        return settings.findAll()
                .stream()
                .collect(Collectors.toMap(
                        Setting::getSetting,
                        Setting::getValue));
    }

    /** RPG: SETUPMAIN/UPDATE; BR-ADMSETTING-02, BR-ADMSETTING-03. */
    public Map<String, String> save(String password, String allowVote) {
        settings.findById("PASSWORD")
                .ifPresent(setting -> {
                    setting.setValue(password);
                    settings.save(setting);
                });
        settings.findById("ALWVOTE")
                .ifPresent(setting -> {
                    setting.setValue(allowVote);
                    settings.save(setting);
                });
        return load();
    }
}
