package com.vcf400.service;

import com.vcf400.domain.Exhibit;
import com.vcf400.domain.ExhibitRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
/** Java implementation of RPG program ADMCRTEXHB. */
public class AdminExhibitService {

    private final ExhibitRepository exhibits;

    public AdminExhibitService(ExhibitRepository exhibits) {
        this.exhibits = exhibits;
    }

    public record ExhibitForm(
            String profile,
            String owner,
            String city,
            String state,
            String title,
            String description,
            int eligible,
            int learn400) {
    }

    public record ExhibitResult(
            boolean success,
            String message,
            int status,
            Exhibit exhibit) {
    }

    public static class ExhibitEditorSession {

        private String edtExhb;
        private int newOrEdt;
        private int errSts;
        private String profile = "";
        private String owner = "";
        private String city = "";
        private String state = "";
        private String title = "";
        private String description = "";
        private int eligible;
        private int learn400;

        public String getEdtExhb() {
            return edtExhb;
        }

        public int getNewOrEdt() {
            return newOrEdt;
        }

        public int getErrSts() {
            return errSts;
        }

        public String getProfile() {
            return profile;
        }

        public String getOwner() {
            return owner;
        }

        public String getCity() {
            return city;
        }

        public String getState() {
            return state;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public int getEligible() {
            return eligible;
        }

        public int getLearn400() {
            return learn400;
        }

        public void setProfile(String profile) {
            this.profile = profile;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public void setState(String state) {
            this.state = state;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setEligible(int eligible) {
            this.eligible = eligible;
        }

        public void setLearn400(int learn400) {
            this.learn400 = learn400;
        }
    }

    /** RPG: BEGSR LOADEXHB; BR-ADMCRTEXHB-01, BR-ADMCRTEXHB-02. */
    public ExhibitEditorSession loadSession(String exhibit) {
        ExhibitEditorSession session = new ExhibitEditorSession();
        session.edtExhb = exhibit;
        if ("NONE".equals(exhibit)) {
            session.newOrEdt = 0;
            return session;
        }

        Exhibit existing = exhibits.findById(exhibit).orElse(null);
        if (existing == null) {
            session.profile = exhibit;
            session.errSts = 2;
            session.newOrEdt = 0;
            return session;
        }

        copyToSession(session, existing);
        session.newOrEdt = 1;
        return session;
    }

    /** RPG: BEGSR LOADEXHB; BR-ADMCRTEXHB-01, BR-ADMCRTEXHB-02. */
    public Exhibit load(String profile) {
        return exhibits.findById(profile).orElse(null);
    }

    /** RPG: BEGSR WRITEEXHB; BR-ADMCRTEXHB-03, BR-ADMCRTEXHB-04, BR-ADMCRTEXHB-05. */
    public ExhibitResult writeExhb(ExhibitForm form) {
        ExhibitEditorSession session = loadSession("NONE");
        if (form != null) {
            copyForm(session, form);
        }
        return writeExhb(session);
    }

    /** RPG: BEGSR WRITEEXHB; BR-ADMCRTEXHB-03, BR-ADMCRTEXHB-04, BR-ADMCRTEXHB-05. */
    public ExhibitResult writeExhb(ExhibitEditorSession session) {
        if (session.profile == null || session.profile.isBlank()) {
            session.errSts = 5;
            return result(session, false, null);
        }

        Exhibit existing = exhibits.findById(session.profile).orElse(null);
        if (session.newOrEdt == 0 && existing != null) {
            session.errSts = 1;
            return result(session, false, existing);
        }

        Exhibit saved;
        if (session.newOrEdt == 1 && existing != null) {
            existing.update(
                    session.owner,
                    session.city,
                    session.state,
                    session.title,
                    session.description,
                    session.eligible,
                    session.learn400);
            saved = exhibits.save(existing);
        } else {
            saved = exhibits.save(new Exhibit(
                    0,
                    session.profile,
                    session.owner,
                    session.city,
                    session.state,
                    session.title,
                    session.description,
                    session.eligible,
                    session.learn400));
        }
        session.errSts = 0;
        return result(session, true, saved);
    }

    /** RPG: BEGSR DELRCD; BR-ADMCRTEXHB-06, BR-ADMCRTEXHB-07. */
    public ExhibitResult delRcd(
            ExhibitEditorSession session,
            boolean confirmed) {
        if (session.profile == null || session.profile.isBlank()) {
            session.errSts = 5;
            return result(session, false, null);
        }
        Exhibit existing = exhibits.findById(session.profile).orElse(null);
        if (existing == null) {
            session.errSts = 3;
            return result(session, false, null);
        }
        if (!confirmed) {
            return result(session, false, existing);
        }
        exhibits.delete(existing);
        session.errSts = 4;
        session.profile = "";
        return result(session, true, existing);
    }

    public ExhibitResult delRcd(String profile, boolean confirmed) {
        ExhibitEditorSession session = loadSession(profile);
        session.profile = profile;
        return delRcd(session, confirmed);
    }

    public List<Exhibit> list() {
        return exhibits.findAll();
    }

    private void copyForm(ExhibitEditorSession session, ExhibitForm form) {
        session.profile = form.profile();
        session.owner = form.owner();
        session.city = form.city();
        session.state = form.state();
        session.title = form.title();
        session.description = form.description();
        session.eligible = form.eligible();
        session.learn400 = form.learn400();
    }

    private void copyToSession(
            ExhibitEditorSession session,
            Exhibit exhibit) {
        session.profile = exhibit.getExhUsrPrf();
        session.owner = exhibit.getExhBitor();
        session.city = exhibit.getExhBcity();
        session.state = exhibit.getExhBstate();
        session.title = exhibit.getExhBtitle();
        session.description = exhibit.getExhBdesc();
        session.eligible = exhibit.getEligible();
        session.learn400 = exhibit.getEnlRn400();
    }

    private ExhibitResult result(
            ExhibitEditorSession session,
            boolean success,
            Exhibit exhibit) {
        return new ExhibitResult(
                success,
                messageFor(session.errSts),
                session.errSts,
                exhibit);
    }

    private String messageFor(int status) {
        return switch (status) {
            case 1 -> RpgMessages.USER_EXISTS;
            case 2 -> RpgMessages.USER_CREATED;
            case 3 -> RpgMessages.NOT_DELETED;
            case 4 -> RpgMessages.DELETED;
            case 5 -> RpgMessages.USER_BLANK;
            default -> null;
        };
    }
}
