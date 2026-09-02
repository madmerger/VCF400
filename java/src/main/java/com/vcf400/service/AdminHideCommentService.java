package com.vcf400.service;

import com.vcf400.domain.*;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program ADMHIDECMT. */
public class AdminHideCommentService {
    private final GuestbookCommentRepository comments;
    public AdminHideCommentService(GuestbookCommentRepository comments) { this.comments=comments; }
    public record HideResult(boolean success,String message,GuestbookComment comment) {}
    /** RPG: BEGSR READDB; BR-ADMHIDECMT-01. */
    public GuestbookComment read(int id) { return comments.findById(id).orElse(null); }
    /** RPG: BEGSR HIDECMTSR/CHKUSRPRF; BR-ADMHIDECMT-02/03/04. */
    public HideResult update(int id,String hideYn) {
        GuestbookComment c=comments.findById(id).orElse(null);
        if(c==null) return new HideResult(false,RpgMessages.COMMENT_ID,null);
        if(!"Y".equals(hideYn)&&!"N".equals(hideYn)) return new HideResult(false,RpgMessages.INVALID_YN,c);
        c.setVisible(hideYn); return new HideResult(true,RpgMessages.STATUS_UPDATED,comments.save(c));
    }
}
