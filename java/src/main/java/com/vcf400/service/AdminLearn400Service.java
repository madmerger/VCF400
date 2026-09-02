package com.vcf400.service;

import com.vcf400.domain.*;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program ADMLRN400. */
public class AdminLearn400Service {
    private final Lrn400PageRepository pages;
    public AdminLearn400Service(Lrn400PageRepository pages) { this.pages=pages; }
    /** RPG: BEGSR PAGEFWD/PAGEBACK; BR-ADMLRN400-01. */
    public Learn400Service.Learn400Session pageFwd(Learn400Service.Learn400Session s) { return new Learn400Service(pages).pageFwd(s); }
    public Learn400Service.Learn400Session pageBack(Learn400Service.Learn400Session s) { return new Learn400Service(pages).pageBack(s); }
    /** RPG: BEGSR WRITERCD/UPDRCD/CRTRCD; BR-ADMLRN400-02/03. */
    public Lrn400Page save(String owner, int page, String content, String extra, boolean newOrUpd) {
        Lrn400PageId id=new Lrn400PageId(owner,page); Lrn400Page p=pages.findById(id).orElse(new Lrn400Page(owner,page,content,extra)); p.update(content,extra); return pages.save(p);
    }
}
