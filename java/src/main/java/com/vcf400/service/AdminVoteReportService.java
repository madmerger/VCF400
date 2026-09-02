package com.vcf400.service;

import com.vcf400.domain.Exhibit;
import com.vcf400.domain.ExhibitRepository;
import com.vcf400.domain.Vote;
import com.vcf400.domain.VoteRepository;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program ADMVOTERPT. */
public class AdminVoteReportService {

    private final ExhibitRepository exhibits;
    private final VoteRepository votes;

    public AdminVoteReportService(
            ExhibitRepository exhibits,
            VoteRepository votes) {
        this.exhibits = exhibits;
        this.votes = votes;
    }

    public record VoteReport(
            long totalVotes,
            long award1Count,
            long award2Count,
            String outTitle,
            String errLine) {
    }

    /** RPG: BEGSR READVOTE; BR-ADMVOTERPT-01, BR-ADMVOTERPT-02, BR-ADMVOTERPT-03, BR-ADMVOTERPT-04. */
    public VoteReport readVote(String profile) {
        if (profile == null || profile.isBlank()) {
            return new VoteReport(0, 0, 0, null, RpgMessages.USER_BLANK);
        }
        Exhibit exhibit = exhibits.findById(profile).orElse(null);
        if (exhibit == null) {
            return new VoteReport(0, 0, 0, null, RpgMessages.USER_NOT_FOUND);
        }

        long award1 = 0;
        long award2 = 0;
        long total = 0;
        for (Vote vote : votes.findAll()) {
            if (!profile.equals(vote.getExhNbr())) {
                continue;
            }
            total++;
            if (vote.getAwardNbr() == 1) {
                award1++;
            }
            if (vote.getAwardNbr() == 2) {
                award2++;
            }
        }
        return new VoteReport(total, award1, award2, exhibit.getExhBtitle(), null);
    }
}
