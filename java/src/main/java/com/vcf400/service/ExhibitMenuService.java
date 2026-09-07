package com.vcf400.service;

import com.vcf400.domain.Exhibit;
import com.vcf400.domain.ExhibitRepository;
import com.vcf400.domain.SettingRepository;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program EXHBMENU. */
public class ExhibitMenuService {

    private final ExhibitRepository exhibits;
    private final SettingRepository settings;

    public ExhibitMenuService(
            ExhibitRepository exhibits,
            SettingRepository settings) {
        this.exhibits = exhibits;
        this.settings = settings;
    }

    public record MenuView(
            String title,
            String name,
            String city,
            String state,
            String desc,
            boolean showVote,
            boolean showLrn400) {
    }

    /** RPG: BEGSR GETEXHB; BR-EXHBMENU-01, BR-EXHBMENU-02. */
    public MenuView load(String profile) {
        profile = DdsField.truncate(profile, 9);
        Exhibit exhibit = exhibits.findById(profile).orElse(null);
        if (exhibit == null) {
            return new MenuView(null, null, null, null, null, false, false);
        }
        return new MenuView(
                exhibit.getExhBtitle(),
                exhibit.getExhBitor(),
                exhibit.getExhBcity(),
                exhibit.getExhBstate(),
                exhibit.getExhBdesc(),
                exhibit.getEligible() == 1,
                exhibit.getEnlRn400() == 1);
    }

    /** RPG: BEGSR DOVOTE/DOLRN400/ADMKIOSK; BR-EXHBMENU-03, BR-EXHBMENU-04, BR-EXHBMENU-06, BR-EXHBMENU-07. */
    public String select(String profile, int option) {
        profile = DdsField.truncate(profile, 9);
        MenuView view = load(profile);
        return switch (option) {
            case 1 -> view.showVote() && chkAlwVote() ? "VOTESTUB" : "MENU";
            case 2 -> view.showLrn400() ? "LRN400STUB" : "MENU";
            case 3 -> "ADDGBSTUB";
            case 4 -> "READGBSTUB";
            case 7 -> "ADMKIOSK";
            default -> "MENU";
        };
    }

    /** RPG: BEGSR CHKALWVOTE; BR-EXHBMENU-04. */
    private boolean chkAlwVote() {
        return settings.findById("ALWVOTE")
                .map(setting -> !"N".equalsIgnoreCase(setting.getValue()))
                .orElse(true);
    }

    /** RPG: BEGSR GETPSWRD; BR-EXHBMENU-07. */
    public boolean exitKiosk(String password) {
        String truncatedPassword = DdsField.truncate(password, 9);
        return settings.findById("PASSWORD")
                .map(setting -> setting.getValue().equals(truncatedPassword))
                .orElse(false);
    }
}
