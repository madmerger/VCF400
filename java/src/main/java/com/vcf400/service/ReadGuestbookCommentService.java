package com.vcf400.service;

import com.vcf400.domain.Exhibit;
import com.vcf400.domain.ExhibitRepository;
import com.vcf400.domain.GuestbookComment;
import com.vcf400.domain.GuestbookCommentRepository;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program READGBCMT. */
public class ReadGuestbookCommentService {

    private final GuestbookCommentRepository comments;
    private final ExhibitRepository exhibits;

    public ReadGuestbookCommentService(
            GuestbookCommentRepository comments,
            ExhibitRepository exhibits) {
        this.comments = comments;
        this.exhibits = exhibits;
    }

    public record CommentView(
            boolean success,
            String outName,
            String outCmt,
            String outTitle,
            String errLine,
            long totalComments) {
    }

    /** RPG: BEGSR CHKPARM; BR-READGBCMT-01. */
    private String chkParm(String profile) {
        return profile == null ? "" : profile;
    }

    /** RPG: BEGSR GETTLCMT; BR-READGBCMT-05. */
    private long getTotalComments() {
        return comments.findAll()
                .stream()
                .mapToInt(GuestbookComment::getCmtId)
                .max()
                .orElse(0);
    }

    /** RPG: BEGSR READDB; BR-READGBCMT-02, BR-READGBCMT-03, BR-READGBCMT-04. */
    public CommentView read(String profile, int id) {
        String userProfile = chkParm(profile);
        long totalComments = getTotalComments();
        if (id == 0) {
            return new CommentView(
                    false,
                    null,
                    null,
                    null,
                    RpgMessages.COMMENT_ID,
                    totalComments);
        }

        GuestbookComment comment = comments.findById(id).orElse(null);
        if (comment == null) {
            return new CommentView(
                    false,
                    null,
                    RpgMessages.PRIVATE_COMMENT,
                    null,
                    userProfile,
                    totalComments);
        }

        if (!"Y".equals(comment.getVisible())) {
            return new CommentView(
                    true,
                    RpgMessages.HIDDEN_NAME,
                    RpgMessages.HIDDEN_COMMENT,
                    exhibitTitle(comment.getExhbId()),
                    userProfile,
                    totalComments);
        }

        if ("MM2024".equals(userProfile)
                || userProfile.equals(comment.getExhbId())) {
            return new CommentView(
                    true,
                    comment.getGuestName(),
                    comment.getGuestCmt(),
                    exhibitTitle(comment.getExhbId()),
                    userProfile,
                    totalComments);
        }

        return new CommentView(
                true,
                "",
                RpgMessages.PRIVATE_COMMENT,
                "",
                userProfile,
                totalComments);
    }

    private String exhibitTitle(String id) {
        return exhibits.findById(id)
                .map(Exhibit::getExhBtitle)
                .orElse("");
    }
}
