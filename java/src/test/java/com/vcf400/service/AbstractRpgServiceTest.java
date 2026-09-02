package com.vcf400.service;

import com.vcf400.domain.Award;
import com.vcf400.domain.AwardRepository;
import com.vcf400.domain.BeeMovieLineRepository;
import com.vcf400.domain.Exhibit;
import com.vcf400.domain.ExhibitRepository;
import com.vcf400.domain.GuestbookCommentRepository;
import com.vcf400.domain.Lrn400Page;
import com.vcf400.domain.Lrn400PageRepository;
import com.vcf400.domain.SecurityOfficerRepository;
import com.vcf400.domain.Setting;
import com.vcf400.domain.SettingRepository;
import com.vcf400.domain.VoteRepository;
import com.vcf400.print.PrintSpoolService;
import org.junit.jupiter.api.BeforeEach;
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
abstract class AbstractRpgServiceTest {

    @Autowired
    protected VoteRepository votes;

    @Autowired
    protected AwardRepository awards;

    @Autowired
    protected ExhibitRepository exhibits;

    @Autowired
    protected GuestbookCommentRepository comments;

    @Autowired
    protected Lrn400PageRepository pages;

    @Autowired
    protected SettingRepository settings;

    @Autowired
    protected SecurityOfficerRepository officers;

    @Autowired
    protected BeeMovieLineRepository beeMovieLines;

    @Autowired
    protected AddVoteService addVote;

    @Autowired
    protected AddGuestbookCommentService addComment;

    @Autowired
    protected ReadGuestbookCommentService readComment;

    @Autowired
    protected Learn400Service learn;

    @Autowired
    protected Learn400AutoService auto;

    @Autowired
    protected AdminLearn400Service adminLearn;

    @Autowired
    protected ExhibitMenuService menu;

    @Autowired
    protected AdminVoteReportService voteReport;

    @Autowired
    protected AdminExhibitService exhibitAdmin;

    @Autowired
    protected AdminHideCommentService hideComment;

    @Autowired
    protected AdminSecurityOfficerService securityOfficer;

    @Autowired
    protected AdminSettingService setting;

    @Autowired
    protected StaticScreenService staticScreens;

    @Autowired
    protected BeeMovieService beeMovie;

    @Autowired
    protected PrintSpoolService spool;

    @BeforeEach
    void seedCommonData() {
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
}
