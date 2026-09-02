package com.vcf400.service;

import com.vcf400.domain.Lrn400Page;
import com.vcf400.domain.Lrn400PageId;
import com.vcf400.domain.Lrn400PageRepository;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program ADMLRN400. */
public class AdminLearn400Service {

    private final Lrn400PageRepository pages;

    public AdminLearn400Service(Lrn400PageRepository pages) {
        this.pages = pages;
    }

    public static class AdminLearnSession {

        private final String owner;
        private int curPageNbr;
        private int alwFwd;
        private int newOrUpd;
        private String inContent = "";
        private String inExtra = "";
        private String outPageNbr = "";
        private boolean in60;

        public AdminLearnSession(String owner) {
            this.owner = owner;
        }

        public String getOwner() {
            return owner;
        }

        public int getCurPageNbr() {
            return curPageNbr;
        }

        public int getAlwFwd() {
            return alwFwd;
        }

        public int getNewOrUpd() {
            return newOrUpd;
        }

        public String getInContent() {
            return inContent;
        }

        public String getInExtra() {
            return inExtra;
        }

        public String getOutPageNbr() {
            return outPageNbr;
        }

        public boolean isIn60() {
            return in60;
        }

        public void setInContent(String value) {
            inContent = value;
        }

        public void setInExtra(String value) {
            inExtra = value;
        }
    }

    /** RPG: initial display; BR-ADMLRN400-01. */
    public AdminLearnSession start(String owner) {
        AdminLearnSession session = new AdminLearnSession(owner);
        session.alwFwd = 1;
        session.newOrUpd = 0;
        session.in60 = true;
        loadFirst(session);
        return session;
    }

    /** RPG: BEGSR PAGEFWD; BR-ADMLRN400-02, BR-ADMLRN400-03. */
    public AdminLearnSession pageFwd(AdminLearnSession session) {
        clearInput(session);
        if (session.alwFwd == 1) {
            session.curPageNbr++;
        }
        Lrn400Page page = chain(session.owner, session.curPageNbr);
        if (page == null) {
            session.alwFwd = 0;
            session.newOrUpd = 1;
            session.outPageNbr = String.valueOf(session.curPageNbr);
            session.in60 = false;
            return session;
        }
        copyPage(session, page);
        return session;
    }

    /** RPG: BEGSR PAGEBACK; BR-ADMLRN400-02, BR-ADMLRN400-03. */
    public AdminLearnSession pageBack(AdminLearnSession session) {
        if (session.curPageNbr != 0) {
            session.curPageNbr--;
        }
        clearInput(session);
        Lrn400Page page = chain(session.owner, session.curPageNbr);
        if (page != null) {
            copyPage(session, page);
        }
        session.alwFwd = 1;
        session.newOrUpd = 0;
        session.in60 = true;
        return session;
    }

    /** RPG: BEGSR WRITERCD/UPDRCD/CRTRCD; BR-ADMLRN400-04, BR-ADMLRN400-05. */
    public AdminLearnSession save(AdminLearnSession session) {
        Lrn400Page page = chain(session.owner, session.curPageNbr);
        if (session.newOrUpd == 0 && page != null) {
            page.update(session.inContent, session.inExtra);
            pages.save(page);
        } else {
            pages.save(new Lrn400Page(
                    session.owner,
                    session.curPageNbr,
                    session.inContent,
                    session.inExtra));
            session.newOrUpd = 0;
        }
        session.alwFwd = 1;
        session.in60 = true;
        return session;
    }

    private void loadFirst(AdminLearnSession session) {
        pages.findByOwnerOrderByPageNbr(session.owner)
                .stream()
                .findFirst()
                .ifPresentOrElse(
                        page -> copyPage(session, page),
                        () -> {
                            session.curPageNbr = 1;
                            session.outPageNbr = "1";
                            session.newOrUpd = 1;
                            session.in60 = false;
                        });
    }

    private void clearInput(AdminLearnSession session) {
        session.inContent = "";
        session.inExtra = "";
    }

    private void copyPage(AdminLearnSession session, Lrn400Page page) {
        session.curPageNbr = page.getPageNbr();
        session.outPageNbr = String.valueOf(page.getPageNbr());
        session.inContent = page.getContent();
        session.inExtra = page.getExtra() == null ? "" : page.getExtra();
        session.newOrUpd = 0;
        session.in60 = true;
    }

    private Lrn400Page chain(String owner, int page) {
        return pages.findById(new Lrn400PageId(owner, page)).orElse(null);
    }
}
