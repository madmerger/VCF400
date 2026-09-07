package com.vcf400.service;

import com.vcf400.domain.Exhibit;
import com.vcf400.domain.Setting;
import com.vcf400.repository.ExhibitRepository;
import com.vcf400.repository.SettingsRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** EXHBMENU.rpgle の移植 (F-07, B-11, B-12)。 */
@Service
public class KioskService {
    private final ExhibitRepository exhibits;
    private final SettingsRepository settings;

    public KioskService(ExhibitRepository exhibits, SettingsRepository settings) {
        this.exhibits = exhibits;
        this.settings = settings;
    }

    /** CHKPARM: EXHBDB by LAUNCH (STREXHB EXHBNAME は存在するプロファイル名前提のため完全一致で解決)。 */
    public Optional<Exhibit> exhibitFor(String launch) {
        return exhibits.findById(launch.trim().toUpperCase());
    }

    /** GETPSWRD: SETTINGS.ADMPSWRD の VALUE。 */
    public String exitPassword() {
        return settings.find(Setting.ADMPSWRD).map(Setting::value).orElse("");
    }

    /** ADMKIOSK: INPWD = EXITPSWRD で終了。 */
    public boolean exitAllowed(String inPwd) {
        String pw = exitPassword();
        return !pw.isEmpty() && pw.equals(inPwd == null ? "" : inPwd.trim());
    }
}
