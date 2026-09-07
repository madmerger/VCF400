package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.vcf400.domain.GuestbookComment;
import com.vcf400.domain.Vote;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PrintSpoolServiceTest extends AbstractRpgServiceTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-PRTLSTVOTE-01: 投票券をスプールへ追加する")
        void printsVoteTicket() {
            spool.printVoteTicket(new Vote(1, 1, "GERTIE"));

            assertThat(spool.spool())
                    .anyMatch(value -> value.contains("PRTLSTVOTE"));
        }

        @Test
        @DisplayName("BR-PRTLSTCMT-01: コメント一覧をスプールへ追加する")
        void printsCommentTicket() {
            spool.printCommentTicket(
                    new GuestbookComment(2, "Y", "GERTIE", "Name", "Comment"));

            assertThat(spool.spool())
                    .anyMatch(value -> value.contains("PRTLSTCMT"));
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-PRTLSTVOTE-02: 投票券にはbadge番号を含める")
        void includesBadgeNumber() {
            spool.printVoteTicket(new Vote(12, 1, "GERTIE"));

            assertThat(spool.spool())
                    .anyMatch(value -> value.contains("BADGENBR=12"));
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-PRTLSTCMT-02: コメント券にはCMTIDを含める")
        void includesCommentId() {
            spool.printCommentTicket(
                    new GuestbookComment(12, "Y", "GERTIE", "Name", "Comment"));

            assertThat(spool.spool())
                    .anyMatch(value -> value.contains("CMTID=12"));
        }
    }
}
