package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.vcf400.domain.Lrn400Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class Learn400ServiceTest extends AbstractRpgServiceTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-LRN400-01: 開始時は1ページ目を表示する")
        void startsAtFirstPage() {
            Learn400Service.Learn400Session session = learn.start("LRN400STR");

            assertThat(session.getCurPageNbr()).isEqualTo(1);
            assertThat(session.getOutContent()).isEqualTo("One");
        }

        @Test
        @DisplayName("BR-LRN400-02: F5で次ページへ進む")
        void movesForward() {
            Learn400Service.Learn400Session session = learn.start("LRN400STR");

            learn.pageFwd(session);

            assertThat(session.getCurPageNbr()).isEqualTo(2);
            assertThat(session.getOutContent()).isEqualTo("Two");
        }

        @Test
        @DisplayName("BR-LRN400-03: CALLを記録し次ページへ進む")
        void recordsCall() {
            pages.save(new Lrn400Page("CALLTEST", 1, "Start", ""));
            pages.save(new Lrn400Page("CALLTEST", 2, "CALL", "NTRSTIT"));
            pages.save(new Lrn400Page("CALLTEST", 3, "After", "END"));

            Learn400Service.Learn400Session session = learn.start("CALLTEST");
            learn.pageFwd(session);

            assertThat(session.getCalledProgram()).isEqualTo("CALL:NTRSTIT");
            assertThat(session.getCurPageNbr()).isEqualTo(3);
        }

        @Test
        @DisplayName("BR-LRN400-04: JUMPはEXTRAのページへ遷移する")
        void jumpsToExtraPage() {
            Learn400Service.Learn400Session session = learn.start("LRN400STR");
            learn.pageFwd(session);
            learn.pageFwd(session);
            learn.pageFwd(session);

            assertThat(session.getCurPageNbr()).isEqualTo(6);
        }

        @Test
        @DisplayName("BR-LRN400-05: ENDページはended=trueにする")
        void marksEndPage() {
            Learn400Service.Learn400Session session = learn.start("LRN400STR");
            learn.pageFwd(session);
            learn.pageFwd(session);
            learn.pageFwd(session);

            assertThat(session.isEnded()).isTrue();
            assertThat(session.getOutContent()).isEqualTo("End");
        }

        @Test
        @DisplayName("BR-LRN400-06: JUMP後のF8はFRMPAGENBRへ戻る")
        void returnsToFromPageAfterJump() {
            Learn400Service.Learn400Session session = learn.start("LRN400STR");
            learn.pageFwd(session);
            learn.pageFwd(session);
            learn.pageFwd(session);

            learn.pageBack(session);

            assertThat(session.getCurPageNbr()).isEqualTo(3);
            assertThat(session.getOutContent()).isEqualTo("Three");
        }

        @Test
        @DisplayName("BR-LRN400-05: ENDページからF8で戻るとended=falseになる")
        void clearsEndedWhenBackingFromEnd() {
            Learn400Service.Learn400Session session = learn.start("LRN400STR");
            learn.pageFwd(session);
            learn.pageFwd(session);
            learn.pageFwd(session);

            assertThat(session.isEnded()).isTrue();

            learn.pageBack(session);

            assertThat(session.isEnded()).isFalse();
            assertThat(session.getOutContent()).isEqualTo("Three");
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-LRN400-07: ページ無しownerはALWFWD=0で内容を空にする")
        void handlesMissingOwner() {
            Learn400Service.Learn400Session session = learn.start("UNKNOWN");

            assertThat(session.getAlwFwd()).isZero();
            assertThat(session.getOutContent()).isEmpty();
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-LRN400-08: 1ページ目でF8を押すとページ0になる")
        void backsFromFirstPage() {
            Learn400Service.Learn400Session session = learn.start("LRN400STR");

            learn.pageBack(session);

            assertThat(session.getCurPageNbr()).isZero();
            assertThat(session.getAlwFwd()).isEqualTo(1);
        }

        @Test
        @DisplayName("BR-LRN400-09: 末尾到達後はALWFWD=0になる")
        void disablesForwardAtMissingNextPage() {
            Learn400Service.Learn400Session session = learn.start("LRN400STR");
            learn.pageFwd(session);
            learn.pageFwd(session);
            learn.pageFwd(session);
            learn.pageFwd(session);

            assertThat(session.getAlwFwd()).isZero();
        }

        @Test
        @DisplayName("BR-LRN400-10: ownerはDDS桁9で切り捨てる")
        void truncatesOwner() {
            Learn400Service.Learn400Session session =
                    learn.start("LRN400STRXXXX");

            assertThat(session.getOwner()).isEqualTo("LRN400STR");
        }
    }
}
