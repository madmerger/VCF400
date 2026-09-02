package com.vcf400.service;

import com.vcf400.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
/** Java implementation of RPG program LRN400. */
public class Learn400Service {
    private static final Logger log=LoggerFactory.getLogger(Learn400Service.class);
    private final Lrn400PageRepository pages;
    public Learn400Service(Lrn400PageRepository pages) { this.pages=pages; }
    public static class Learn400Session {
        private String owner; private int curPageNbr, frmPageNbr; private String pageAction, outPageNbr, outContent, calledProgram; private boolean alwFwd, ended;
        public Learn400Session(String owner) { this.owner=owner; }
        public String getOwner(){return owner;} public int getCurPageNbr(){return curPageNbr;} public int getFrmPageNbr(){return frmPageNbr;}
        public String getPageAction(){return pageAction;} public String getOutPageNbr(){return outPageNbr;} public String getOutContent(){return outContent;}
        public String getCalledProgram(){return calledProgram;} public boolean isAlwFwd(){return alwFwd;} public boolean isEnded(){return ended;}
    }
    /** RPG: BEGSR CHKPARM/PARSESCRIPT; BR-LRN400-01/02. */
    public Learn400Session start(String owner) { Learn400Session s=new Learn400Session(owner==null||owner.isBlank()?"LRN400STR":owner); s.frmPageNbr=0; return load(s,1); }
    /** RPG: BEGSR PAGEFWD; BR-LRN400-03. */
    public Learn400Session pageFwd(Learn400Session s) { return load(s, s.curPageNbr+1); }
    /** RPG: BEGSR PAGEBACK; BR-LRN400-04. */
    public Learn400Session pageBack(Learn400Session s) { return load(s, Math.max(1,s.curPageNbr-1)); }
    private Learn400Session load(Learn400Session s,int page) {
        List<Lrn400Page> all=pages.findByOwnerOrderByPageNbr(s.owner);
        if(all.isEmpty()) { s.ended=true; s.outContent=RpgMessages.END; return s; }
        Lrn400Page p=pages.findById(new Lrn400PageId(s.owner,page)).orElse(all.get(Math.min(page-1,all.size()-1)));
        s.curPageNbr=p.getPageNbr(); s.outPageNbr=String.valueOf(p.getPageNbr()); s.outContent=p.getContent(); s.pageAction=p.getExtra(); s.alwFwd=p.getPageNbr()<all.get(all.size()-1).getPageNbr(); s.ended="END".equals(p.getExtra());
        if("JUMP".equals(p.getContent()) || "JUMP".equals(p.getExtra())) { s.calledProgram="JUMP:"+p.getExtra(); log.info("LRN400: {}",s.calledProgram); }
        if("CALL".equals(p.getContent()) || "CALL".equals(p.getExtra())) { s.calledProgram="CALL:"+p.getExtra(); log.info("LRN400: {}",s.calledProgram); }
        return s;
    }
}
