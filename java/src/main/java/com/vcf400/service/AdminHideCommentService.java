package com.vcf400.service;

import com.vcf400.domain.GuestbookComment;
import com.vcf400.domain.GuestbookCommentRepository;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program ADMHIDECMT. */
public class AdminHideCommentService {

    private final GuestbookCommentRepository comments;

    public AdminHideCommentService(GuestbookCommentRepository comments) {
        this.comments = comments;
    }

    public record HideResult(
            boolean success,
            String message,
            GuestbookComment comment) {
    }

    /** RPG: BEGSR READDB; BR-ADMHIDECMT-01. */
    public GuestbookComment read(int id) {
        return comments.findById(id).orElse(null);
    }

    public java.util.List<GuestbookComment> list() {
        return comments.findAll();
    }

    /** RPG: BEGSR HIDECMTSR/CHKUSRPRF; BR-ADMHIDECMT-02, BR-ADMHIDECMT-03, BR-ADMHIDECMT-04. */
    public HideResult update(int id, String hideYn) {
        GuestbookComment comment = comments.findById(id).orElse(null);
        if (comment == null) {
            return new HideResult(false, RpgMessages.COMMENT_ID, null);
        }
        if (!"Y".equals(hideYn) && !"N".equals(hideYn)) {
            return new HideResult(false, RpgMessages.INVALID_YN, comment);
        }
        comment.setVisible(hideYn);
        return new HideResult(
                true,
                RpgMessages.STATUS_UPDATED,
                comments.save(comment));
    }
}
