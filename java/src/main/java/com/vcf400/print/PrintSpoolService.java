package com.vcf400.print;

import com.vcf400.domain.GuestbookComment;
import com.vcf400.domain.Vote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrintSpoolService {
    private static final Logger log = LoggerFactory.getLogger(PrintSpoolService.class);
    private final List<String> entries = new ArrayList<>();
    public synchronized void printVoteTicket(Vote vote) {
        String text = "PRTLSTVOTE: BADGENBR=" + vote.getBadgeNbr() + " AWARDNBR=" + vote.getAwardNbr() + " EXHBNBR=" + vote.getExhNbr();
        entries.add(text); log.info(text);
    }
    public synchronized void printCommentTicket(GuestbookComment comment) {
        String text = "PRTLSTCMT: CMTID=" + comment.getCmtId() + " EXHBID=" + comment.getExhbId() + " GUESTNAME=" + comment.getGuestName();
        entries.add(text); log.info(text);
    }
    public synchronized List<String> spool() { return List.copyOf(entries); }
}
