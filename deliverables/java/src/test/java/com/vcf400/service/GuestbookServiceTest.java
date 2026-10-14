package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.vcf400.domain.GuestbookComment;
import com.vcf400.domain.Launch;
import com.vcf400.repository.GuestbookRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** ADDGBCMT / READGBCMT 業務ルール (B-06..B-09, V-11..V-14)。 */
@SpringBootTest
@ActiveProfiles("test")
class GuestbookServiceTest {
    private static final Launch OWN = new Launch("ASHIBATA");
    private static final Launch OTHER = new Launch("DEMO400");
    private static final Launch SHARED = new Launch(Launch.MM2024);

    @Autowired GuestbookService service;
    @Autowired GuestbookRepository comments;

    @Test
    void requiredFieldsInSourceOrderLastErrorWins() {   // V-11..V-13: 展示→名前→コメント、ERRLINE 後勝ち
        GuestbookService.AddResult r = service.add(SHARED, "", "", "");
        assertThat(r.errLine()).isEqualTo(Messages.ERRNOCMT);
        assertThat(r.exhibitErr()).isTrue();
        assertThat(r.nameErr()).isTrue();
        assertThat(r.commentErr()).isTrue();

        assertThat(service.add(SHARED, "", "", "hi").errLine()).isEqualTo(Messages.ERRNONAME);
        assertThat(service.add(SHARED, "Bob", "", "hi").errLine()).isEqualTo(Messages.ERRBLKEX);
        assertThat(service.add(OWN, "", "", "hi").errLine()).isEqualTo(Messages.ERRNONAME);
    }

    @Test
    void newCommentGetsLastIdPlusOneAndVisibleY() {     // B-06, B-07
        int last = comments.findLast().map(GuestbookComment::cmtid).orElse(0);
        GuestbookService.AddResult r = service.add(OWN, "Tester", "", "Great exhibit");
        assertThat(r.hasError()).isFalse();
        assertThat(r.added().cmtid()).isEqualTo(last + 1);
        assertThat(r.added().visible()).isEqualTo("Y");
        assertThat(r.added().exhbid()).isEqualTo("ASHIBATA");
        comments.deleteById(r.added().cmtid());
    }

    @Test
    void readOwnComment() {                             // F-05 正常
        Optional<GuestbookService.ReadResult> out = service.read(OWN, 1);
        assertThat(out).isPresent();
        assertThat(out.get().outName()).isEqualTo("Great exhib");        // OUTNAME 11A (DDS) で切詰め
        assertThat(out.get().outTitle()).isEqualTo("IBM i on PUB400 Demo");
        assertThat(out.get().outCmt()).isEqualTo("VCF/400 running on PUB400.");
    }

    @Test
    void hiddenCommentShowsReplacementText() {          // B-08
        comments.updateVisible(1, "N");
        try {
            GuestbookService.ReadResult out = service.read(OWN, 1).orElseThrow();
            assertThat(out.outName()).isEqualTo(Messages.ERRHNAME);
            assertThat(out.outCmt()).isEqualTo(Messages.ERRHCMT);
        } finally {
            comments.updateVisible(1, "Y");
        }
    }

    @Test
    void foreignExhibitCommentIsPrivate() {             // B-09
        GuestbookService.ReadResult out = service.read(OTHER, 1).orElseThrow();
        assertThat(out.outCmt()).isEqualTo(Messages.ERRPRIV);
        assertThat(out.outName()).isEmpty();
    }

    @Test
    void sharedTerminalCanReadAnyExhibitComment() {     // B-09 MM2024
        GuestbookService.ReadResult out = service.read(SHARED, 2).orElseThrow();
        assertThat(out.outName()).isEqualTo("Devin");
    }

    @Test
    void idBeyondLastRecordFallsBackToLastRecord() {    // B-15 (SETLL/READ EOF 時のレガシー挙動)
        int last = comments.findLast().map(GuestbookComment::cmtid).orElse(0);
        GuestbookService.ReadResult out = service.read(SHARED, last + 500).orElseThrow();
        assertThat(out.record().cmtid()).isEqualTo(last);
    }
}
