package com.vcf400.service;

import com.vcf400.domain.Lrn400PageRepository;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program LRN400AUT. */
public class Learn400AutoService {
    private final Lrn400PageRepository pages;
    public Learn400AutoService(Lrn400PageRepository pages) { this.pages=pages; }
    /** RPG: BEGSR PAGEFWD; BR-LRN400AUT-01/02. */
    public Learn400Service.Learn400Session start(String owner) { return new Learn400Service(pages).start(owner); }
    /** RPG: END wraps to page zero; BR-LRN400AUT-03. */
    public Learn400Service.Learn400Session pageFwd(Learn400Service.Learn400Session s) {
        if(s.isEnded()) return start(s.getOwner());
        return new Learn400Service(pages).pageFwd(s);
    }
    /** RPG: BEGSR PAGEBACK; BR-LRN400AUT-04. */
    public Learn400Service.Learn400Session pageBack(Learn400Service.Learn400Session s) { return new Learn400Service(pages).pageBack(s); }
}
