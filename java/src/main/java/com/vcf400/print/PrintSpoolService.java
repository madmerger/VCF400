package com.vcf400.print;

import java.util.ArrayList;
import java.util.List;

import com.vcf400.domain.GuestbookComment;
import com.vcf400.domain.Vote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PrintSpoolService {

    private static final Logger log = LoggerFactory.getLogger(PrintSpoolService.class);

    private final List<String> entries = new ArrayList<>();

    /** RPG: PRTLSTVOTE/PRINTER output; BR-PRTLSTVOTE-01, BR-PRTLSTVOTE-03. */
    public synchronized void printVoteTicket(Vote vote) {
        String text =
                "PRTLSTVOTE: BADGENBR="
                        + vote.getBadgeNbr()
                        + " AWARDNBR="
                        + vote.getAwardNbr()
                        + " EXHBNBR="
                        + vote.getExhNbr();
        entries.add(text);
        log.info(text);
    }

    /** RPG: PRTLSTCMT output; BR-PRTLSTVOTE-02, BR-PRTLSTVOTE-03. */
    public synchronized void printCommentTicket(GuestbookComment comment) {
        String text =
                "PRTLSTCMT: CMTID="
                        + comment.getCmtId()
                        + " EXHBID="
                        + comment.getExhbId()
                        + " GUESTNAME="
                        + comment.getGuestName();
        entries.add(text);
        log.info(text);
    }

    public synchronized List<String> spool() {
        return List.copyOf(entries);
    }
}
