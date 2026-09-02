package com.vcf400.service;

import com.vcf400.domain.*;
import org.springframework.stereotype.Service;

@Service
/** Java implementation of RPG program ADMVOTERPT. */
public class AdminVoteReportService {
    private final ExhibitRepository exhibits; private final VoteRepository votes;
    public AdminVoteReportService(ExhibitRepository exhibits, VoteRepository votes) { this.exhibits=exhibits; this.votes=votes; }
    public record VoteReport(long totalVotes,long award1Count,long award2Count,String outTitle,String errLine) {}
    /** RPG: BEGSR READVOTE; BR-ADMVOTERPT-01/02/03. */
    public VoteReport readVote(String profile) {
        if(profile==null||profile.isBlank()) return new VoteReport(0,0,0,null,RpgMessages.USER_BLANK);
        Exhibit e=exhibits.findById(profile).orElse(null); if(e==null) return new VoteReport(0,0,0,null,RpgMessages.USER_NOT_FOUND);
        return new VoteReport(votes.countByExhNbr(profile),votes.findAll().stream().filter(v->v.getExhNbr().equals(profile)&&v.getAwardNbr()==1).count(),votes.findAll().stream().filter(v->v.getExhNbr().equals(profile)&&v.getAwardNbr()==2).count(),e.getExhBtitle(),null);
    }
}
