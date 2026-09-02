package com.vcf400.service;

import com.vcf400.domain.Lrn400Page;
import com.vcf400.domain.Lrn400PageId;
import com.vcf400.domain.Lrn400PageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
/** Java implementation of RPG program LRN400. */
public class Learn400Service {

    private static final Logger log = LoggerFactory.getLogger(Learn400Service.class);

    private final Lrn400PageRepository pages;

    public Learn400Service(Lrn400PageRepository pages) {
        this.pages = pages;
    }

    public static class Learn400Session {

        private final String owner;
        private int curPageNbr;
        private int frmPageNbr;
        private int pageAction;
        private int alwFwd;
        private String outPageNbr = "";
        private String outContent = "";
        private String calledProgram = "";
        private boolean ended;

        public Learn400Session(String owner) {
            this.owner = owner;
        }

        public String getOwner() {
            return owner;
        }

        public int getCurPageNbr() {
            return curPageNbr;
        }

        public int getFrmPageNbr() {
            return frmPageNbr;
        }

        public int getPageAction() {
            return pageAction;
        }

        public int getAlwFwd() {
            return alwFwd;
        }

        public String getOutPageNbr() {
            return outPageNbr;
        }

        public String getOutContent() {
            return outContent;
        }

        public String getCalledProgram() {
            return calledProgram;
        }

        public boolean isEnded() {
            return ended;
        }

        void setCurPageNbr(int value) {
            curPageNbr = value;
        }

        void setFrmPageNbr(int value) {
            frmPageNbr = value;
        }

        void setPageAction(int value) {
            pageAction = value;
        }

        void setAlwFwd(int value) {
            alwFwd = value;
        }

        void setOutPageNbr(String value) {
            outPageNbr = value;
        }

        void setOutContent(String value) {
            outContent = value;
        }

        void setEnded(boolean value) {
            ended = value;
        }
    }

    /** RPG: initial READ and CHKPARM; BR-LRN400-01. */
    public Learn400Session start(String owner) {
        String actualOwner = owner == null || owner.isBlank() ? "LRN400STR" : owner;
        Learn400Session session = new Learn400Session(actualOwner);
        session.alwFwd = 1;
        session.frmPageNbr = 0;
        Lrn400Page first = pages.findByOwnerOrderByPageNbr(actualOwner)
                .stream()
                .findFirst()
                .orElse(null);
        if (first == null) {
            session.alwFwd = 0;
            session.outContent = "";
            return session;
        }
        session.curPageNbr = first.getPageNbr();
        session.outPageNbr = String.valueOf(first.getPageNbr());
        session.outContent = first.getContent();
        return session;
    }

    /** RPG: BEGSR PAGEFWD; BR-LRN400-02, BR-LRN400-03. */
    public Learn400Session pageFwd(Learn400Session session) {
        return PAGEFWD(session);
    }

    /** RPG: BEGSR PAGEBACK; BR-LRN400-04, BR-LRN400-05. */
    public Learn400Session pageBack(Learn400Session session) {
        return PAGEBACK(session);
    }

    /** RPG: BEGSR PAGEFWD; BR-LRN400-02, BR-LRN400-03. */
    private Learn400Session PAGEFWD(Learn400Session session) {
        session.outContent = "";
        session.calledProgram = "";
        if (session.alwFwd == 1) {
            session.curPageNbr++;
            session.frmPageNbr = 0;
        }

        Lrn400Page page = chain(session.owner, session.curPageNbr);
        if (page == null) {
            session.alwFwd = 0;
            return session;
        }

        setOutput(session, page);
        PARSESCRIPT(session, page);
        PRFRMACTN(session, page);
        return session;
    }

    /** RPG: BEGSR PAGEBACK; BR-LRN400-04, BR-LRN400-05. */
    private Learn400Session PAGEBACK(Learn400Session session) {
        if (session.curPageNbr != 0) {
            session.curPageNbr--;
        }
        session.outContent = "";
        int target = session.frmPageNbr == 0
                ? session.curPageNbr
                : session.frmPageNbr;
        Lrn400Page page = chain(session.owner, target);
        if (page != null) {
            setOutput(session, page);
        }
        session.alwFwd = 1;
        session.frmPageNbr = 0;
        return session;
    }

    /** RPG: BEGSR PARSESCRIPT; BR-LRN400-03. */
    private void PARSESCRIPT(Learn400Session session, Lrn400Page page) {
        session.pageAction = 0;
        if ("END".equals(page.getExtra())) {
            session.pageAction = 1;
        }
        if ("CALL".equals(session.outContent)) {
            session.pageAction = 2;
        }
        if ("JUMP".equals(session.outContent)) {
            session.pageAction = 3;
        }
    }

    /** RPG: BEGSR PRFRMACTN; BR-LRN400-03, BR-LRN400-04. */
    private void PRFRMACTN(Learn400Session session, Lrn400Page page) {
        if (session.pageAction == 1) {
            session.ended = true;
            return;
        }
        if (session.pageAction == 2) {
            session.calledProgram = "CALL:" + page.getExtra();
            log.info("LRN400: {}", session.calledProgram);
            JUMPTO(session, page);
            session.pageAction = 0;
        }
        if (session.pageAction == 3) {
            JUMPTO(session, page);
            session.pageAction = 0;
        }
    }

    /** RPG: BEGSR JUMPTO; BR-LRN400-03, BR-LRN400-04. */
    private void JUMPTO(Learn400Session session, Lrn400Page page) {
        if (session.pageAction == 2) {
            session.frmPageNbr = session.curPageNbr - 1;
            session.curPageNbr++;
        }
        if (session.pageAction == 3) {
            session.frmPageNbr = session.curPageNbr - 1;
            try {
                session.curPageNbr = Integer.parseInt(page.getExtra());
            } catch (NumberFormatException ignored) {
                session.alwFwd = 0;
                session.outContent = "";
                return;
            }
        }
        Lrn400Page target = chain(session.owner, session.curPageNbr);
        if (target == null) {
            session.alwFwd = 0;
            session.outContent = "";
            return;
        }
        setOutput(session, target);
        session.alwFwd = 1;
        if ("END".equals(target.getExtra())) {
            session.ended = true;
        }
    }

    private Lrn400Page chain(String owner, int pageNbr) {
        return pages.findById(new Lrn400PageId(owner, pageNbr)).orElse(null);
    }

    private void setOutput(Learn400Session session, Lrn400Page page) {
        session.outPageNbr = String.valueOf(page.getPageNbr());
        session.outContent = page.getContent();
    }
}
