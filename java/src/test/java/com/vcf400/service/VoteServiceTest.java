package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.vcf400.domain.Launch;
import com.vcf400.repository.SettingsRepository;
import com.vcf400.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** ADDVOTE 業務ルール (仕様書 B-01..B-05, V-01..V-07) の検証。 */
@SpringBootTest
@ActiveProfiles("test")
class VoteServiceTest {
    private static final Launch OWN = new Launch("ASHIBATA");
    private static final Launch SHARED = new Launch(Launch.MM2024);

    @Autowired VoteService service;
    @Autowired VoteRepository votes;
    @Autowired SettingsRepository settings;

    @BeforeEach
    void reset() {
        settings.update("ALWVOTE", "Y");
        for (int b : new int[] {7001, 7002, 7003, 7004, 7005, 7006}) {
            votes.deleteByBadge(b);
        }
    }

    @Test
    void emptyBadgeIsRejected() {                       // V-01
        VoteService.Result r = service.submit(OWN, 0, "ASHIBATA", 1);
        assertThat(r.errLine()).isEqualTo(Messages.ERRBLKBG);
        assertThat(r.badgeErr()).isTrue();
        assertThat(r.recorded()).isFalse();
    }

    @Test
    void emptyAwardIsRejected() {                       // V-02
        VoteService.Result r = service.submit(OWN, 7001, "ASHIBATA", 0);
        assertThat(r.errLine()).isEqualTo(Messages.ERRBLKAW);
        assertThat(r.awardErr()).isTrue();
    }

    @Test
    void emptyExhibitIsRejectedAndLastErrorWins() {     // V-03: ERRLINE は後勝ち (バッジ→アワード→展示)
        VoteService.Result r = service.submit(SHARED, 0, "", 0);
        assertThat(r.errLine()).isEqualTo(Messages.ERRBLKEX);
        assertThat(r.badgeErr()).isTrue();
        assertThat(r.awardErr()).isTrue();
        assertThat(r.exhibitErr()).isTrue();
    }

    @Test
    void successfulVoteIsWrittenOnlyWhenAllFourChecksPass() {   // B-01 CHECKOK=4
        VoteService.Result r = service.submit(OWN, 7002, "ASHIBATA", 1);
        assertThat(r.recorded()).isTrue();
        assertThat(votes.findByBadge(7002)).isPresent();
        assertThat(votes.findByBadge(7002).get().exhbnbr()).isEqualTo("ASHIBATA");
        assertThat(votes.findByBadge(7002).get().awardnbr()).isEqualTo(1);
    }

    @Test
    void duplicateBadgeIsRejectedEvenWithDifferentAward() {    // B-02 / V-04
        service.submit(OWN, 7003, "ASHIBATA", 1);
        VoteService.Result r = service.submit(OWN, 7003, "ASHIBATA", 2);
        assertThat(r.errLine()).isEqualTo(Messages.ERREXIST);
        assertThat(r.recorded()).isFalse();
        assertThat(votes.findByBadge(7003).get().awardnbr()).isEqualTo(1);
    }

    @Test
    void ineligibleExhibitIsRejected() {                // B-03 / V-05
        VoteService.Result r = service.submit(SHARED, 7004, "NOVOTE", 1);
        assertThat(r.errLine()).isEqualTo(Messages.ERRPROHB);
        assertThat(votes.findByBadge(7004)).isEmpty();
    }

    @Test
    void unknownExhibitIsRejected() {                   // V-06
        VoteService.Result r = service.submit(SHARED, 7005, "NOSUCH", 1);
        assertThat(r.errLine()).isEqualTo(Messages.ERRNOEXB);
        assertThat(votes.findByBadge(7005)).isEmpty();
    }

    @Test
    void unknownAwardIsRejected() {                     // V-07
        VoteService.Result r = service.submit(OWN, 7006, "ASHIBATA", 999);
        assertThat(r.errLine()).isEqualTo(Messages.ERRNOAWD);
        assertThat(votes.findByBadge(7006)).isEmpty();
    }

    @Test
    void ownerLaunchForcesExhibitToSignedOnProfile() {  // B-05: 通常起動は LAUNCH で展示 ID 固定
        assertThat(service.protectedExhibit(OWN)).isEqualTo("ASHIBATA");
        VoteService.Result r = service.submit(OWN, 7001, "DEMO400", 1);
        assertThat(r.recorded()).isTrue();
        assertThat(votes.findByBadge(7001).get().exhbnbr()).isEqualTo("ASHIBATA");
    }

    @Test
    void votingClosedWhenAlwvoteIsN() {                 // B-04
        settings.update("ALWVOTE", "N");
        assertThat(service.isVotingAllowed()).isFalse();
        settings.update("ALWVOTE", "Y");
        assertThat(service.isVotingAllowed()).isTrue();
    }
}
