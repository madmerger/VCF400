package com.vcf400.service;

import com.vcf400.domain.Lrn400Page;
import com.vcf400.domain.Lrn400PageId;
import com.vcf400.domain.Lrn400PageRepository;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program LRN400AUT. */
public class Learn400AutoService {

    private final Lrn400PageRepository pages;

    public Learn400AutoService(Lrn400PageRepository pages) {
        this.pages = pages;
    }

    /** RPG: initial READ and WAITRCD loop; BR-LRN400AUT-01. */
    public Learn400Service.Learn400Session start(String owner) {
        String actualOwner = owner == null || owner.isBlank()
                ? "LRN400STR"
                : DdsField.truncate(owner, 9);
        Learn400Service.Learn400Session session =
                new Learn400Service.Learn400Session(actualOwner);
        session.setAlwFwd(1);
        Lrn400Page first = pages.findByOwnerOrderByPageNbr(actualOwner)
                .stream()
                .findFirst()
                .orElse(null);
        if (first == null) {
            session.setAlwFwd(0);
            return session;
        }
        session.setCurPageNbr(first.getPageNbr());
        setOutput(session, first);
        return session;
    }

    /** RPG: BEGSR PAGEFWD; BR-LRN400AUT-01, BR-LRN400AUT-02, BR-LRN400AUT-03. */
    public Learn400Service.Learn400Session pageFwd(Learn400Service.Learn400Session s) {
        if (s.isEnded()) {
            Learn400Service.Learn400Session first = start(s.getOwner());
            s.setCurPageNbr(first.getCurPageNbr());
            s.setFrmPageNbr(first.getFrmPageNbr());
            s.setPageAction(first.getPageAction());
            s.setAlwFwd(first.getAlwFwd());
            s.setOutPageNbr(first.getOutPageNbr());
            s.setOutContent(first.getOutContent());
            s.setEnded(false);
            return s;
        }
        if (s.getAlwFwd() == 1) {
            s.setCurPageNbr(s.getCurPageNbr() + 1);
        }
        Lrn400Page page = pages.findById(
                new Lrn400PageId(s.getOwner(), s.getCurPageNbr())).orElse(null);
        if (page == null) {
            s.setAlwFwd(0);
            s.setOutContent("");
            return s;
        }
        setOutput(s, page);
        if ("END".equals(page.getExtra())) {
            s.setEnded(true);
            s.setCurPageNbr(0);
            s.setAlwFwd(1);
        }
        return s;
    }

    /** RPG: BEGSR PAGEBACK; BR-LRN400AUT-04. */
    public Learn400Service.Learn400Session pageBack(
            Learn400Service.Learn400Session s) {
        Learn400Service.Learn400Session previous =
                new Learn400Service(pages).pageBack(s);
        previous.setEnded(false);
        return previous;
    }

    private void setOutput(
            Learn400Service.Learn400Session session,
            Lrn400Page page) {
        session.setOutPageNbr(String.valueOf(page.getPageNbr()));
        session.setOutContent(page.getContent());
        session.setPageAction(0);
    }
}
