package com.vcf400.service;

import com.vcf400.domain.AwardRepository;
import com.vcf400.domain.Exhibit;
import com.vcf400.domain.ExhibitRepository;
import com.vcf400.domain.SettingRepository;
import com.vcf400.domain.Vote;
import com.vcf400.domain.VoteRepository;
import com.vcf400.print.PrintSpoolService;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program ADDVOTE. */
public class AddVoteService {

    private final VoteRepository votes;
    private final ExhibitRepository exhibits;
    private final AwardRepository awards;
    private final SettingRepository settings;
    private final PrintSpoolService printer;

    public AddVoteService(
            VoteRepository votes,
            ExhibitRepository exhibits,
            AwardRepository awards,
            SettingRepository settings,
            PrintSpoolService printer) {
        this.votes = votes;
        this.exhibits = exhibits;
        this.awards = awards;
        this.settings = settings;
        this.printer = printer;
    }

    public record VoteResult(
            boolean success,
            String errLine,
            String screen) {
    }

    private record Input(
            String profile,
            int badge,
            String exhibit,
            int award) {
    }

    private record Validation(String error, String screen) {
    }

    /** RPG: BEGSR CHKPARM; BR-ADDVOTE-01, BR-ADDVOTE-02. */
    private String chkParm(String profile, String exhibit) {
        if ("MM2024".equals(profile)) {
            return exhibit;
        }
        return profile;
    }

    /** RPG: BEGSR CHKALWVOTE; BR-ADDVOTE-04. */
    private boolean chkAlwVote() {
        return settings.findById("ALWVOTE")
                .map(setting -> !"N".equalsIgnoreCase(setting.getValue()))
                .orElse(true);
    }

    /** RPG: input validation; BR-ADDVOTE-01, BR-ADDVOTE-02, BR-ADDVOTE-03, BR-ADDVOTE-09. */
    private Validation validate(Input input) {
        String error = null;
        if (input.badge() == 0) {
            error = RpgMessages.BADGE;
        }
        if (input.award() == 0) {
            error = RpgMessages.AWARD;
        }
        if (input.exhibit() == null || input.exhibit().isBlank()) {
            error = RpgMessages.EXHIBIT;
        }
        if (error != null) {
            return new Validation(error, "VOTE1");
        }
        if ("DAVE".equals(input.exhibit())) {
            return new Validation(null, "DAVE");
        }
        return new Validation(null, null);
    }

    /** RPG: BEGSR ADDTODB; BR-ADDVOTE-05, BR-ADDVOTE-06, BR-ADDVOTE-07, BR-ADDVOTE-08. */
    private VoteResult addToDb(Input input) {
        if (votes.existsById(input.badge())) {
            return new VoteResult(false, RpgMessages.EXISTS, "VOTE1");
        }

        Exhibit exhibit = exhibits.findById(input.exhibit()).orElse(null);
        if (exhibit == null) {
            return new VoteResult(false, RpgMessages.NO_EXHIBIT, "VOTE1");
        }
        if (exhibit.getEligible() == 0) {
            return new VoteResult(false, RpgMessages.INELIGIBLE, "VOTE1");
        }
        if (!awards.existsById(input.award())) {
            return new VoteResult(false, RpgMessages.NO_AWARD, "VOTE1");
        }

        Vote saved = votes.save(new Vote(input.badge(), input.award(), input.exhibit()));
        printer.printVoteTicket(saved);
        return new VoteResult(true, RpgMessages.VOTED, "VOTEEND");
    }

    /**
     * Executes the RPG ADDVOTE flow.
     *
     * @param launchProfile current exhibit or privileged profile
     * @param badge badge number
     * @param exhibitId exhibit identifier
     * @param award award identifier
     * @return RPG-compatible result screen and ERRLINE
     */
    public VoteResult submit(String launchProfile, int badge, String exhibitId, int award) {
        if (!chkAlwVote()) {
            return new VoteResult(false, null, "ENDOFCON");
        }

        Input input = new Input(
                launchProfile,
                badge,
                chkParm(launchProfile, exhibitId),
                award);
        Validation validation = validate(input);
        if (validation.error() != null || validation.screen() != null) {
            return new VoteResult(false, validation.error(), validation.screen());
        }
        return addToDb(input);
    }
}
