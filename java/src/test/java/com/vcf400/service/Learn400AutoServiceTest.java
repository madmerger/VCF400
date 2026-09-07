package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.vcf400.domain.Lrn400Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class Learn400AutoServiceTest extends AbstractRpgServiceTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-LRN400AUT-01: 自動表示は先頭ページから開始する")
        void startsAtFirstPage() {
            Learn400Service.Learn400Session session = auto.start("LRN400STR");

            assertThat(session.getCurPageNbr()).isEqualTo(1);
        }

        @Test
        @DisplayName("BR-LRN400AUT-02: END後は先頭ページへラップする")
        void wrapsAfterEnd() {
            pages.deleteAll();
            pages.save(new Lrn400Page("AUTO", 1, "One", ""));
            pages.save(new Lrn400Page("AUTO", 2, "Two", "END"));

            Learn400Service.Learn400Session session = auto.start("AUTO");
            auto.pageFwd(session);
            auto.pageFwd(session);

            assertThat(session.getCurPageNbr()).isEqualTo(1);
            assertThat(session.isEnded()).isFalse();
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-LRN400AUT-03: JUMPは自動進行では解釈しない")
        void ignoresJumpScript() {
            pages.deleteAll();
            pages.save(new Lrn400Page("AUTO", 1, "One", ""));
            pages.save(new Lrn400Page("AUTO", 2, "JUMP", "0008"));

            Learn400Service.Learn400Session session = auto.start("AUTO");
            auto.pageFwd(session);

            assertThat(session.getCurPageNbr()).isEqualTo(2);
            assertThat(session.getOutContent()).isEqualTo("JUMP");
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-LRN400AUT-04: CALLを外部呼出しせず次ページへ進む")
        void ignoresCallScript() {
            pages.deleteAll();
            pages.save(new Lrn400Page("AUTO", 1, "One", ""));
            pages.save(new Lrn400Page("AUTO", 2, "CALL", "NTRSTIT"));
            pages.save(new Lrn400Page("AUTO", 3, "Three", ""));

            Learn400Service.Learn400Session session = auto.start("AUTO");
            auto.pageFwd(session);

            assertThat(session.getCurPageNbr()).isEqualTo(2);
            assertThat(session.getCalledProgram()).isEmpty();
        }
    }
}
