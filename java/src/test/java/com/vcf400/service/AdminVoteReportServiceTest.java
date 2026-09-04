package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.vcf400.domain.Vote;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AdminVoteReportServiceTest extends AbstractRpgServiceTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-ADMVOTERPT-01: Votes in DBは全出展者の票を合計する")
        void countsAllVotes() {
            votes.save(new Vote(1, 1, "GERTIE"));
            votes.save(new Vote(2, 2, "OTHER"));

            assertThat(voteReport.readVote("GERTIE").totalVotes()).isEqualTo(2);
        }

        @Test
        @DisplayName("BR-ADMVOTERPT-02: Best in Show票を集計する")
        void countsAwardOne() {
            votes.save(new Vote(1, 1, "GERTIE"));

            assertThat(voteReport.readVote("GERTIE").award1Count()).isEqualTo(1);
        }

        @Test
        @DisplayName("BR-ADMVOTERPT-03: Ed Fair票を集計する")
        void countsAwardTwo() {
            votes.save(new Vote(1, 2, "GERTIE"));

            assertThat(voteReport.readVote("GERTIE").award2Count()).isEqualTo(1);
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-ADMVOTERPT-04: 不在profileはUSRPRF was not foundを返す")
        void handlesMissingProfile() {
            assertThat(voteReport.readVote("MISSING").errLine())
                    .isEqualTo(RpgMessages.USER_NOT_FOUND);
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-ADMVOTERPT-05: profileはDDS桁9で切り捨てる")
        void truncatesProfile() {
            assertThat(voteReport.readVote("MISSINGXXXX").errLine())
                    .isEqualTo(RpgMessages.USER_NOT_FOUND);
        }
    }
}
