package com.vcf400.service;

import com.vcf400.domain.*;
import com.vcf400.print.PrintSpoolService;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program ADDVOTE. */
public class AddVoteService {
    private final VoteRepository votes; private final ExhibitRepository exhibits; private final AwardRepository awards;
    private final SettingRepository settings; private final PrintSpoolService printer;
    public AddVoteService(VoteRepository votes, ExhibitRepository exhibits, AwardRepository awards, SettingRepository settings, PrintSpoolService printer) {
        this.votes=votes; this.exhibits=exhibits; this.awards=awards; this.settings=settings; this.printer=printer;
    }
    public record VoteResult(boolean success, String errLine, String screen) {}
    /** RPG: BEGSR CHKPARM; BR-ADDVOTE-01/02. */
    private String chkParm(String profile, String exhibit) { return "MM2024".equals(profile) ? exhibit : profile; }
    /** RPG: BEGSR CHKALWVOTE; BR-ADDVOTE-03. */
    private boolean chkAlwVote() { return settings.findById("ALWVOTE").map(s -> !"N".equalsIgnoreCase(s.getValue())).orElse(true); }
    /** RPG: main validation; BR-ADDVOTE-04/05/06/07. */
    public VoteResult submit(String launchProfile, int badge, String exhibitId, int award) {
        if (!chkAlwVote()) return new VoteResult(false, null, "ENDOFCON");
        String exhibit = chkParm(launchProfile, exhibitId);
        String err = null;
        if (badge == 0) err = RpgMessages.BADGE;
        if (award == 0) err = RpgMessages.AWARD;
        if (exhibit == null || exhibit.isBlank()) err = RpgMessages.EXHIBIT;
        if (err != null) return new VoteResult(false, err, "VOTE1");
        if ("DAVE".equals(exhibit)) return new VoteResult(false, null, "DAVE");
        if (votes.existsById(badge)) return new VoteResult(false, RpgMessages.EXISTS, "VOTE1");
        Exhibit ex = exhibits.findById(exhibit).orElse(null);
        if (ex == null) return new VoteResult(false, RpgMessages.NO_EXHIBIT, "VOTE1");
        if (ex.getEligible() == 0) return new VoteResult(false, RpgMessages.INELIGIBLE, "VOTE1");
        if (!awards.existsById(award)) return new VoteResult(false, RpgMessages.NO_AWARD, "VOTE1");
        Vote vote = votes.save(new Vote(badge, award, exhibit)); printer.printVoteTicket(vote);
        return new VoteResult(true, RpgMessages.VOTED, "VOTEEND");
    }
}
