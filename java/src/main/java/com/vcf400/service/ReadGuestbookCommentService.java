package com.vcf400.service;

import com.vcf400.domain.*;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program READGBCMT. */
public class ReadGuestbookCommentService {
    private final GuestbookCommentRepository comments; private final ExhibitRepository exhibits;
    public ReadGuestbookCommentService(GuestbookCommentRepository comments, ExhibitRepository exhibits) { this.comments=comments; this.exhibits=exhibits; }
    public record CommentView(boolean success, String outName, String outCmt, String outTitle, String errLine, long totalComments) {}
    /** RPG: BEGSR GETTLCMT; BR-READGBCMT-01. */
    public CommentView read(String profile, int id) {
        long total=comments.count();
        if (id == 0) return new CommentView(false, null, null, null, RpgMessages.COMMENT_ID, total);
        GuestbookComment c=comments.findById(id).orElse(null);
        if (c == null) return new CommentView(false, null, null, null, RpgMessages.PRIVATE_COMMENT, total);
        if (!"MM2024".equals(profile) && !profile.equals(c.getExhbId())) return new CommentView(false, null, RpgMessages.PRIVATE_COMMENT, null, null, total);
        if (!"Y".equals(c.getVisible())) return new CommentView(true, RpgMessages.HIDDEN_NAME, RpgMessages.HIDDEN_COMMENT, exhibitTitle(c.getExhbId()), null, total);
        return new CommentView(true, c.getGuestName(), c.getGuestCmt(), exhibitTitle(c.getExhbId()), null, total);
    }
    private String exhibitTitle(String id) { return exhibits.findById(id).map(Exhibit::getExhBtitle).orElse(null); }
}
