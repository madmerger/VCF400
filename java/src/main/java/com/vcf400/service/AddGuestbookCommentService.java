package com.vcf400.service;

import com.vcf400.domain.GuestbookComment;
import com.vcf400.domain.GuestbookCommentRepository;
import com.vcf400.print.PrintSpoolService;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program ADDGBCMT. */
public class AddGuestbookCommentService {

    private final GuestbookCommentRepository comments;
    private final PrintSpoolService printer;

    public AddGuestbookCommentService(
            GuestbookCommentRepository comments,
            PrintSpoolService printer) {
        this.comments = comments;
        this.printer = printer;
    }

    public record CommentResult(
            boolean success,
            String errLine,
            Integer commentId,
            String screen) {
    }

    /** RPG: BEGSR CHKPARM; BR-ADDGBCMT-01, BR-ADDGBCMT-02, BR-ADDGBCMT-03, BR-ADDGBCMT-04, BR-ADDGBCMT-05. */
    public CommentResult submit(
            String profile,
            String exhibitId,
            String name,
            String comment) {
        String exhibit = "MM2024".equals(profile) ? exhibitId : profile;
        String err = null;
        if (exhibit == null || exhibit.isBlank()) {
            err = RpgMessages.COMMENT_EXHIBIT;
        }
        if (name == null || name.isBlank()) {
            err = RpgMessages.NO_NAME;
        }
        if (comment == null || comment.isBlank()) {
            err = RpgMessages.NO_COMMENT;
        }
        if (err != null) {
            return new CommentResult(false, err, null, "ADDCMT");
        }

        int id = comments.findAll().stream()
                .mapToInt(GuestbookComment::getCmtId)
                .max()
                .orElse(0) + 1;
        GuestbookComment saved = comments.save(
                new GuestbookComment(id, "Y", exhibit, name, comment));
        printer.printCommentTicket(saved);
        return new CommentResult(true, RpgMessages.THANKS, id, "ENDCMT");
    }
}
