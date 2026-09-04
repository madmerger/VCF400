package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AddVoteServiceTest extends AbstractRpgServiceTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-ADDVOTE-05: 有効な投票をDBへ書き込みスプールへ出力する")
        void savesVoteAndPrintsTicket() {
            AddVoteService.VoteResult result =
                    addVote.submit("GERTIE", 1, "GERTIE", 1);

            assertThat(result.success()).isTrue();
            assertThat(votes.count()).isEqualTo(1);
            assertThat(spool.spool())
                    .anyMatch(value -> value.contains("BADGENBR=1"));
        }

        @Test
        @DisplayName("BR-ADDVOTE-06: DAVEは投票登録せずDAVE画面を返す")
        void daveDoesNotSaveVote() {
            AddVoteService.VoteResult result =
                    addVote.submit("MM2024", 2, "DAVE", 1);

            assertThat(result.screen()).isEqualTo("DAVE");
            assertThat(votes.count()).isZero();
        }

        @Test
        @DisplayName("BR-ADDVOTE-08: MM2024は入力した出展者へ投票する")
        void mm2024AllowsFreeExhibitInput() {
            AddVoteService.VoteResult result =
                    addVote.submit("MM2024", 3, "GERTIE", 1);

            assertThat(result.success()).isTrue();
            assertThat(votes.findById(3).orElseThrow().getExhNbr())
                    .isEqualTo("GERTIE");
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-ADDVOTE-05: 同一バッジの再投票は拒否する")
        void rejectsDuplicateBadge() {
            addVote.submit("GERTIE", 4, "GERTIE", 1);

            assertThat(addVote.submit("GERTIE", 4, "GERTIE", 1).errLine())
                    .isEqualTo(RpgMessages.EXISTS);
        }

        @Test
        @DisplayName("BR-ADDVOTE-01: badge=0はバッジ番号必須エラー")
        void rejectsMissingBadge() {
            assertThat(addVote.submit("MM2024", 0, "GERTIE", 1).errLine())
                    .isEqualTo(RpgMessages.BADGE);
        }

        @Test
        @DisplayName("BR-ADDVOTE-02: award=0は賞必須エラー")
        void rejectsMissingAward() {
            assertThat(addVote.submit("MM2024", 5, "GERTIE", 0).errLine())
                    .isEqualTo(RpgMessages.AWARD);
        }

        @Test
        @DisplayName("BR-ADDVOTE-03: exhibit空欄は出展ID必須エラー")
        void rejectsMissingExhibit() {
            assertThat(addVote.submit("MM2024", 5, "", 1).errLine())
                    .isEqualTo(RpgMessages.EXHIBIT);
        }

        @Test
        @DisplayName("BR-ADDVOTE-01/02/03: 必須エラーは後勝ちでEXHIBITが最優先")
        void requiredErrorsUseLastError() {
            AddVoteService.VoteResult result =
                    addVote.submit("MM2024", 0, "", 0);

            assertThat(result.errLine()).isEqualTo(RpgMessages.EXHIBIT);
        }

        @Test
        @DisplayName("BR-ADDVOTE-07: 存在しない出展者は拒否する")
        void rejectsMissingExhibitRecord() {
            assertThat(addVote.submit("MM2024", 6, "MISSING", 1).errLine())
                    .isEqualTo(RpgMessages.NO_EXHIBIT);
        }

        @Test
        @DisplayName("BR-ADDVOTE-07: eligible=0の出展者は拒否する")
        void rejectsIneligibleExhibit() {
            assertThat(addVote.submit("MM2024", 7, "NOVOTE", 1).errLine())
                    .isEqualTo(RpgMessages.INELIGIBLE);
        }

        @Test
        @DisplayName("BR-ADDVOTE-08: 存在しない賞は拒否する")
        void rejectsMissingAwardRecord() {
            assertThat(addVote.submit("GERTIE", 8, "GERTIE", 9).errLine())
                    .isEqualTo(RpgMessages.NO_AWARD);
        }

        @Test
        @DisplayName("BR-ADDVOTE-04: ALWVOTE=Nは投票終了画面を返す")
        void rejectsWhenVotingDisabled() {
            settings.findById("ALWVOTE").orElseThrow().setValue("N");

            assertThat(addVote.submit("GERTIE", 9, "GERTIE", 1).screen())
                    .isEqualTo("ENDOFCON");
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-ADDVOTE-09: 非MM2024の出展IDはlaunch profileへ強制する")
        void forcesExhibitForNonMmProfile() {
            AddVoteService.VoteResult result =
                    addVote.submit("GERTIE", 10, "NOVOTE", 1);

            assertThat(result.success()).isTrue();
            assertThat(votes.findById(10).orElseThrow().getExhNbr())
                    .isEqualTo("GERTIE");
        }

        @Test
        @DisplayName("BR-ADDVOTE-10: 長いprofileと出展IDはDDS桁で切り捨てる")
        void truncatesDdsFields() {
            assertThat(addVote.submit(
                    "GERTIEXXXX",
                    11,
                    "GERTIEXXXX",
                    1).errLine()).isEqualTo(RpgMessages.NO_EXHIBIT);
        }
    }
}
