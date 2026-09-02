package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.vcf400.domain.Award;
import com.vcf400.domain.AwardRepository;
import com.vcf400.domain.BeeMovieLine;
import com.vcf400.domain.BeeMovieLineRepository;
import com.vcf400.domain.Exhibit;
import com.vcf400.domain.ExhibitRepository;
import com.vcf400.domain.GuestbookComment;
import com.vcf400.domain.GuestbookCommentRepository;
import com.vcf400.domain.Lrn400Page;
import com.vcf400.domain.Lrn400PageRepository;
import com.vcf400.domain.SecurityOfficerRepository;
import com.vcf400.domain.Setting;
import com.vcf400.domain.SettingRepository;
import com.vcf400.domain.Vote;
import com.vcf400.domain.VoteRepository;
import com.vcf400.print.PrintSpoolService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
        AddVoteService.class,
        AddGuestbookCommentService.class,
        ReadGuestbookCommentService.class,
        Learn400Service.class,
        Learn400AutoService.class,
        AdminLearn400Service.class,
        ExhibitMenuService.class,
        AdminVoteReportService.class,
        AdminExhibitService.class,
        AdminHideCommentService.class,
        AdminSecurityOfficerService.class,
        AdminSettingService.class,
        BeeMovieService.class,
        StaticScreenService.class,
        PrintSpoolService.class
})
class RpgServiceTest {

    @Autowired
    private VoteRepository votes;

    @Autowired
    private AwardRepository awards;

    @Autowired
    private ExhibitRepository exhibits;

    @Autowired
    private GuestbookCommentRepository comments;

    @Autowired
    private Lrn400PageRepository pages;

    @Autowired
    private SettingRepository settings;

    @Autowired
    private SecurityOfficerRepository officers;

    @Autowired
    private BeeMovieLineRepository beeMovieLines;

    @Autowired
    private AddVoteService addVote;

    @Autowired
    private AddGuestbookCommentService addComment;

    @Autowired
    private ReadGuestbookCommentService readComment;

    @Autowired
    private Learn400Service learn;

    @Autowired
    private Learn400AutoService auto;

    @Autowired
    private AdminLearn400Service adminLearn;

    @Autowired
    private ExhibitMenuService menu;

    @Autowired
    private AdminVoteReportService voteReport;

    @Autowired
    private AdminExhibitService exhibitAdmin;

    @Autowired
    private AdminHideCommentService hideComment;

    @Autowired
    private AdminSecurityOfficerService securityOfficer;

    @Autowired
    private AdminSettingService setting;

    @Autowired
    private StaticScreenService staticScreens;

    @Autowired
    private BeeMovieService beeMovie;

    @Autowired
    private PrintSpoolService spool;

    @BeforeEach
    void setUp() {
        awards.save(new Award(1, "Best in Show Award", ""));
        awards.save(new Award(2, "The Ed Fair Award", ""));
        exhibits.save(new Exhibit(
                1,
                "GERTIE",
                "Gertie",
                "Boston",
                "MA",
                "Gertie title",
                "Gertie description",
                1,
                1));
        exhibits.save(new Exhibit(
                2,
                "NOVOTE",
                "No Vote",
                "Austin",
                "TX",
                "No vote title",
                "No vote description",
                0,
                0));
        settings.save(new Setting("ALWVOTE", "Y"));
        settings.save(new Setting("PASSWORD", "VCF400"));
        pages.save(new Lrn400Page("LRN400STR", 1, "One", ""));
        pages.save(new Lrn400Page("LRN400STR", 2, "Two", ""));
        pages.save(new Lrn400Page("LRN400STR", 3, "Three", ""));
        pages.save(new Lrn400Page("LRN400STR", 4, "JUMP", "0006"));
        pages.save(new Lrn400Page("LRN400STR", 6, "End", "END"));
    }

    @Test
    @DisplayName("BR-ADDVOTE-05/06/07/08: 投票成功、重複、適格性、賞存在、スプール")
    void voteSuccessAndDatabaseBranches() {
        AddVoteService.VoteResult result =
                addVote.submit("GERTIE", 1, "GERTIE", 1);

        assertThat(result.success()).isTrue();
        assertThat(votes.count()).isEqualTo(1);
        assertThat(spool.spool())
                .anyMatch(value -> value.contains("BADGENBR=1"));
        assertThat(addVote.submit("GERTIE", 1, "GERTIE", 1).errLine())
                .isEqualTo(RpgMessages.EXISTS);
        assertThat(addVote.submit("MM2024", 2, "NOVOTE", 1).errLine())
                .isEqualTo(RpgMessages.INELIGIBLE);
        assertThat(addVote.submit("MM2024", 3, "MISSING", 1).errLine())
                .isEqualTo(RpgMessages.NO_EXHIBIT);
        assertThat(addVote.submit("GERTIE", 4, "GERTIE", 9).errLine())
                .isEqualTo(RpgMessages.NO_AWARD);
    }

    @Test
    @DisplayName("BR-ADDVOTE-01/02/03: 必須エラーの後勝ち優先順位")
    void voteRequiredFieldsUseLastError() {
        AddVoteService.VoteResult result =
                addVote.submit("MM2024", 0, "", 0);

        assertThat(result.errLine()).isEqualTo(RpgMessages.EXHIBIT);
        assertThat(result.screen()).isEqualTo("VOTE1");
    }

    @Test
    @DisplayName("BR-ADDVOTE-04/09: 投票停止、DAVE、プロフィール保護、桁切捨て")
    void voteSpecialBranchesAndTruncation() {
        settings.findById("ALWVOTE").orElseThrow().setValue("N");
        AddVoteService.VoteResult stopped =
                addVote.submit("GERTIE", 10, "GERTIE", 1);
        assertThat(stopped.screen()).isEqualTo("ENDOFCON");

        settings.findById("ALWVOTE").orElseThrow().setValue("Y");
        AddVoteService.VoteResult dave =
                addVote.submit("MM2024", 11, "DAVE", 1);
        assertThat(dave.screen()).isEqualTo("DAVE");
        assertThat(votes.count()).isZero();

        AddVoteService.VoteResult forced =
                addVote.submit("MM2024", 12, "NOVOTE", 1);
        assertThat(forced.errLine()).isEqualTo(RpgMessages.INELIGIBLE);

        AddVoteService.VoteResult privileged =
                addVote.submit("MM2024", 13, "GERTIEXXXX", 1);
        assertThat(privileged.errLine()).isEqualTo(RpgMessages.NO_EXHIBIT);
    }

    @Test
    @DisplayName("BR-ADDGBCMT-01/02/03/04/05: コメント登録、採番、VISIBLE、入口桁")
    void addGuestbookBranches() {
        AddGuestbookCommentService.CommentResult result =
                addComment.submit(
                        "GERTIE",
                        "NOVOTE",
                        "12345678901234567890",
                        "comment");

        assertThat(result.success()).isTrue();
        assertThat(result.commentId()).isEqualTo(1);
        GuestbookComment saved = comments.findById(1).orElseThrow();
        assertThat(saved.getExhbId()).isEqualTo("GERTIE");
        assertThat(saved.getGuestName()).hasSize(16);
        assertThat(saved.getVisible()).isEqualTo("Y");
        assertThat(spool.spool())
                .anyMatch(value -> value.contains("CMTID=1"));

        comments.save(new GuestbookComment(7, "Y", "GERTIE", "Name", "Text"));
        AddGuestbookCommentService.CommentResult next =
                addComment.submit("GERTIE", "GERTIE", "Name", "Text");
        assertThat(next.commentId()).isEqualTo(8);
    }

    @Test
    @DisplayName("BR-ADDGBCMT-01/02/03: コメント必須項目とMM2024自由入力")
    void addGuestbookRequiredFields() {
        assertThat(addComment.submit("MM2024", "", "Name", "Text").errLine())
                .isEqualTo(RpgMessages.COMMENT_EXHIBIT);
        assertThat(addComment.submit("GERTIE", "", "Name", "Text").errLine())
                .isEqualTo(RpgMessages.THANKS);
        assertThat(addComment.submit("GERTIE", "GERTIE", "", "Text").errLine())
                .isEqualTo(RpgMessages.NO_NAME);
        assertThat(addComment.submit("GERTIE", "GERTIE", "Name", "").errLine())
                .isEqualTo(RpgMessages.NO_COMMENT);
        AddGuestbookCommentService.CommentResult privileged =
                addComment.submit("MM2024", "GERTIE", "Name", "Text");
        assertThat(privileged.success()).isTrue();
    }

    @Test
    @DisplayName("BR-READGBCMT-01/02/03/04/05: コメント公開範囲、エラー、最大ID")
    void readGuestbookBranches() {
        comments.save(new GuestbookComment(4, "Y", "GERTIE", "Alice", "Hello"));
        comments.save(new GuestbookComment(9, "N", "GERTIE", "Bob", "Hidden"));

        assertThat(readComment.read("GERTIE", 0).errLine())
                .isEqualTo(RpgMessages.COMMENT_ID);
        assertThat(readComment.read("GERTIE", 9).outCmt())
                .isEqualTo(RpgMessages.HIDDEN_COMMENT);
        assertThat(readComment.read("BEEBOX", 4).outCmt())
                .isEqualTo(RpgMessages.PRIVATE_COMMENT);
        assertThat(readComment.read("MM2024", 4).outName())
                .isEqualTo("Alice");
        assertThat(readComment.read("GERTIE", 99).outCmt())
                .isEqualTo(RpgMessages.PRIVATE_COMMENT);
        assertThat(readComment.read("GERTIE", 4).totalComments()).isEqualTo(9);
    }

    @Test
    @DisplayName("BR-LRN400-01/02/03/04/05: 状態機械、JUMP、FRMPAGENBR、末尾、空ページ")
    void learnStateMachine() {
        Learn400Service.Learn400Session session = learn.start("LRN400STR");
        assertThat(session.getCurPageNbr()).isEqualTo(1);
        learn.pageFwd(session);
        learn.pageFwd(session);
        Learn400Service.Learn400Session jumped = learn.pageFwd(session);
        assertThat(jumped.getCurPageNbr()).isEqualTo(6);
        assertThat(jumped.getFrmPageNbr()).isEqualTo(3);
        assertThat(jumped.isEnded()).isTrue();
        learn.pageBack(jumped);
        assertThat(jumped.getCurPageNbr()).isEqualTo(3);

        Learn400Service.Learn400Session first = learn.start("LRN400STR");
        learn.pageBack(first);
        assertThat(first.getCurPageNbr()).isZero();
        assertThat(first.getAlwFwd()).isEqualTo(1);

        Learn400Service.Learn400Session missing = learn.start("UNKNOWN");
        assertThat(missing.getAlwFwd()).isZero();
        assertThat(missing.getOutContent()).isEmpty();
    }

    @Test
    @DisplayName("BR-LRN400-03: CALLを記録し、次ページへ遷移")
    void learnCallBranch() {
        pages.save(new Lrn400Page("CALLTEST", 1, "Start", ""));
        pages.save(new Lrn400Page("CALLTEST", 2, "CALL", "NTRSTIT"));
        pages.save(new Lrn400Page("CALLTEST", 3, "After", "END"));

        Learn400Service.Learn400Session session = learn.start("CALLTEST");
        learn.pageFwd(session);

        assertThat(session.getCalledProgram()).isEqualTo("CALL:NTRSTIT");
        assertThat(session.getCurPageNbr()).isEqualTo(3);
    }

    @Test
    @DisplayName("BR-LRN400AUT-01/02/03/04: ENDラップとJUMP/CALL無効")
    void automaticLearnWrapsAtEnd() {
        pages.save(new Lrn400Page("AUTO", 1, "One", ""));
        pages.save(new Lrn400Page("AUTO", 2, "JUMP", "0008"));
        pages.save(new Lrn400Page("AUTO", 3, "Three", "END"));

        Learn400Service.Learn400Session session = auto.start("AUTO");
        auto.pageFwd(session);
        assertThat(session.getCurPageNbr()).isEqualTo(2);
        assertThat(session.getOutContent()).isEqualTo("JUMP");
        auto.pageFwd(session);
        assertThat(session.getCurPageNbr()).isZero();
        assertThat(session.isEnded()).isTrue();
        auto.pageFwd(session);
        assertThat(session.getCurPageNbr()).isEqualTo(1);
    }

    @Test
    @DisplayName("BR-ADMLRN400-01/02/03/04/05: 更新、新規、NEW表示、後退")
    void adminLearnUpdatesAndCreates() {
        AdminLearn400Service.AdminLearnSession session =
                adminLearn.start("LRN400STR");
        session.setInContent("Updated");
        adminLearn.save(session);
        assertThat(pages.findById(new com.vcf400.domain.Lrn400PageId(
                "LRN400STR", 1)).orElseThrow().getContent()).isEqualTo("Updated");

        adminLearn.pageFwd(session);
        adminLearn.pageFwd(session);
        adminLearn.pageFwd(session);
        assertThat(session.getCurPageNbr()).isEqualTo(4);
        adminLearn.pageFwd(session);
        assertThat(session.getNewOrUpd()).isEqualTo(1);
        assertThat(session.isIn60()).isFalse();
        session.setInContent("New page");
        adminLearn.save(session);
        assertThat(pages.findById(new com.vcf400.domain.Lrn400PageId(
                "LRN400STR", 5)).orElseThrow().getContent()).isEqualTo("New page");
        adminLearn.pageBack(session);
        assertThat(session.getNewOrUpd()).isZero();
    }

    @Test
    @DisplayName("BR-EXHBMENU-01/02/03/04/05/06/07: 表示制御、選択、ALWVOTE、PASSWORD")
    void exhibitMenuBranches() {
        ExhibitMenuService.MenuView view = menu.load("GERTIE");
        assertThat(view.showVote()).isTrue();
        assertThat(view.showLrn400()).isTrue();
        assertThat(menu.load("NOVOTE").showVote()).isFalse();
        assertThat(menu.select("GERTIE", 1)).isEqualTo("VOTESTUB");
        assertThat(menu.select("GERTIE", 2)).isEqualTo("LRN400STUB");
        assertThat(menu.select("GERTIE", 3)).isEqualTo("ADDGBSTUB");
        assertThat(menu.select("GERTIE", 4)).isEqualTo("READGBSTUB");
        assertThat(menu.select("GERTIE", 7)).isEqualTo("ADMKIOSK");
        settings.findById("ALWVOTE").orElseThrow().setValue("N");
        assertThat(menu.select("GERTIE", 1)).isEqualTo("MENU");
        assertThat(menu.load("MISSING").showVote()).isFalse();
        assertThat(menu.exitKiosk("VCF400")).isTrue();
        assertThat(menu.exitKiosk("wrong")).isFalse();
    }

    @Test
    @DisplayName("BR-ADMVOTERPT-01/02/03/04: 全票数、賞別集計、不在")
    void voteReportBranches() {
        votes.save(new Vote(1, 1, "GERTIE"));
        votes.save(new Vote(2, 2, "OTHER"));
        AdminVoteReportService.VoteReport report = voteReport.readVote("GERTIE");
        assertThat(report.totalVotes()).isEqualTo(2);
        assertThat(report.award1Count()).isEqualTo(1);
        assertThat(report.award2Count()).isZero();
        assertThat(voteReport.readVote("MISSING").errLine())
                .isEqualTo(RpgMessages.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("BR-ADMCRTEXHB-01/02/03/04/05/06/07: 新規、更新、重複、削除状態")
    void exhibitAdministrationBranches() {
        assertThat(exhibitAdmin.loadSession("NONE").getNewOrEdt()).isZero();
        assertThat(exhibitAdmin.loadSession("GERTIE").getNewOrEdt()).isEqualTo(1);
        assertThat(exhibitAdmin.loadSession("MISSING").getErrSts()).isEqualTo(2);

        AdminExhibitService.ExhibitEditorSession duplicate =
                exhibitAdmin.loadSession("NONE");
        duplicate.setProfile("GERTIE");
        assertThat(exhibitAdmin.writeExhb(duplicate).status()).isEqualTo(1);

        AdminExhibitService.ExhibitEditorSession update =
                exhibitAdmin.loadSession("GERTIE");
        update.setTitle("Updated title");
        assertThat(exhibitAdmin.writeExhb(update).success()).isTrue();
        assertThat(exhibits.findById("GERTIE").orElseThrow().getExhBtitle())
                .isEqualTo("Updated title");

        AdminExhibitService.ExhibitEditorSession blank =
                exhibitAdmin.loadSession("NONE");
        assertThat(exhibitAdmin.writeExhb(blank).status()).isEqualTo(5);
        assertThat(exhibitAdmin.delRcd("MISSING", false).status()).isEqualTo(3);
        assertThat(exhibitAdmin.delRcd("GERTIE", false).status()).isZero();
        assertThat(exhibitAdmin.delRcd("GERTIE", true).status()).isEqualTo(4);
    }

    @Test
    @DisplayName("BR-ADMHIDECMT-01/02/03/04: Y/N/その他と不在ID")
    void hideCommentBranches() {
        comments.save(new GuestbookComment(1, "Y", "GERTIE", "Name", "Text"));
        assertThat(hideComment.update(1, "N").success()).isTrue();
        assertThat(hideComment.read(1).getVisible()).isEqualTo("N");
        assertThat(hideComment.update(1, "Y").success()).isTrue();
        assertThat(hideComment.update(1, "X").message())
                .isEqualTo(RpgMessages.INVALID_YN);
        assertThat(hideComment.update(9, "Y").message())
                .isEqualTo(RpgMessages.COMMENT_ID);
    }

    @Test
    @DisplayName("BR-ADMADDSOFR-01/02: 管理者追加と重複")
    void securityOfficerBranches() {
        assertThat(securityOfficer.add("MM2024"))
                .isEqualTo(RpgMessages.STATUS_UPDATED);
        assertThat(securityOfficer.add("MM2024"))
                .isEqualTo(RpgMessages.USER_EXISTS);
        assertThat(securityOfficer.add("")).isEqualTo(RpgMessages.USER_BLANK);
        assertThat(officers.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("BR-ADMSETTING-01/02/03: Y/Nと1/0の設定変換")
    void settingBranches() {
        assertThat(setting.load().get("ALWVOTE")).isEqualTo("Y");
        setting.save("LONGPASSWORD", "0");
        assertThat(settings.findById("PASSWORD").orElseThrow().getValue())
                .isEqualTo("LONGPASSW");
        assertThat(settings.findById("ALWVOTE").orElseThrow().getValue())
                .isEqualTo("N");
        setting.save("VCF400", "X");
        assertThat(settings.findById("ALWVOTE").orElseThrow().getValue())
                .isEqualTo("X");
    }

    @Test
    @DisplayName("BR-BEEMOVIE-01/BR-CREDITS-01/BR-NTRSTIT-01/BR-PARAMETER-01: 固定出力")
    void staticProgramsReturnDefinedOutput() {
        assertThat(staticScreens.credits().get("text")).contains("VCF/400");
        assertThat(staticScreens.help().get("text")).contains("RIGHT CTRL");
        assertThat(staticScreens.parameters(
                "123456789012345678901",
                "second").get("first")).hasSize(20);
    }

    @Test
    @DisplayName("BR-BEEMOVIE-01/02: BEEMOVIE全件出力")
    void beeMovieReturnsAllLines() {
        beeMovieLines.save(new BeeMovieLine("Speaker 2", "02"));
        beeMovieLines.save(new BeeMovieLine("Speaker 1", "01"));

        assertThat(beeMovie.all())
                .extracting(BeeMovieLine::getLine)
                .containsExactly("01", "02");
    }

    @Test
    @DisplayName("BR-PRTLSTVOTE-01/02/03: 投票券・コメント印刷スプール")
    void printSpoolContainsBothFormats() {
        Vote vote = new Vote(1, 1, "GERTIE");
        GuestbookComment comment =
                new GuestbookComment(2, "Y", "GERTIE", "Name", "Comment");
        spool.printVoteTicket(vote);
        spool.printCommentTicket(comment);
        List<String> entries = spool.spool();
        assertThat(entries).anyMatch(value -> value.contains("PRTLSTVOTE"));
        assertThat(entries).anyMatch(value -> value.contains("PRTLSTCMT"));
    }

    @Test
    @DisplayName("DDS: DdsField.truncateはnullと境界を保持する")
    void ddsFieldBoundaries() {
        assertThat(DdsField.truncate(null, 3)).isNull();
        assertThat(DdsField.truncate("abc", 3)).isEqualTo("abc");
        assertThat(DdsField.truncate("abcd", 3)).isEqualTo("abc");
    }
}
