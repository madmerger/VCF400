package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** LRN400 (B-10) と EXHBMENU (B-11, B-12)。 */
@SpringBootTest
@ActiveProfiles("test")
class LearnKioskServiceTest {
    @Autowired LearnService learn;
    @Autowired KioskService kiosk;

    @Test
    void forwardBackAndEndPage() {
        LearnService.State s = learn.start();
        assertThat(s.outPageNbr()).isEqualTo("1");
        assertThat(s.exit()).isFalse();

        s = learn.forward(s);                             // F5 → page 2
        assertThat(s.outPageNbr()).isEqualTo("2");
        assertThat(s.outContent()).contains("AS/400");

        LearnService.State back = learn.back(s);          // F8 → page 1
        assertThat(back.outPageNbr()).isEqualTo("1");

        LearnService.State end = learn.forward(s);        // F5 → page 3 (EXTRA='END') → exit
        assertThat(end.exit()).isTrue();
    }

    @Test
    void backOnFirstPageStaysOnFirstPage() {
        LearnService.State s = learn.back(learn.start());
        assertThat(s.outPageNbr()).isEqualTo("1");
        assertThat(s.exit()).isFalse();
    }

    @Test
    void kioskExhibitFlags() {
        assertThat(kiosk.exhibitFor("ASHIBATA")).get()
            .satisfies(e -> { assertThat(e.isEligible()).isTrue(); assertThat(e.isLearnEnabled()).isTrue(); });
        assertThat(kiosk.exhibitFor("DEMO400")).get()
            .satisfies(e -> { assertThat(e.isEligible()).isTrue(); assertThat(e.isLearnEnabled()).isFalse(); });
        assertThat(kiosk.exhibitFor("NOVOTE")).get()
            .satisfies(e -> assertThat(e.isEligible()).isFalse());
        assertThat(kiosk.exhibitFor("NOSUCH")).isEmpty();
    }

    @Test
    void kioskExitPasswordComesFromFirstSettingsRecord() {   // B-11: SETLL/READ SETTINGS → ADMPSWRD
        assertThat(kiosk.exitPassword()).isEqualTo("VCF2024");
        assertThat(kiosk.exitAllowed("VCF2024")).isTrue();
        assertThat(kiosk.exitAllowed("vcf2024")).isFalse();
        assertThat(kiosk.exitAllowed("")).isFalse();
    }
}
