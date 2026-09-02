package com.vcf400.web;

import com.vcf400.domain.Award;
import com.vcf400.domain.AwardRepository;
import com.vcf400.domain.Exhibit;
import com.vcf400.domain.ExhibitRepository;
import com.vcf400.domain.GuestbookCommentRepository;
import com.vcf400.domain.Lrn400Page;
import com.vcf400.domain.Lrn400PageRepository;
import com.vcf400.domain.Setting;
import com.vcf400.domain.SettingRepository;
import com.vcf400.domain.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractWebIntegrationTest {

    @Autowired
    protected TestRestTemplate rest;

    @Autowired
    protected AwardRepository awards;

    @Autowired
    protected ExhibitRepository exhibits;

    @Autowired
    protected SettingRepository settings;

    @Autowired
    protected Lrn400PageRepository pages;

    @Autowired
    protected GuestbookCommentRepository comments;

    @Autowired
    protected VoteRepository votes;

    @BeforeEach
    void seedWebData() {
        votes.deleteAll();
        comments.deleteAll();
        pages.deleteAll();
        settings.deleteAll();
        exhibits.deleteAll();
        awards.deleteAll();
        awards.save(new Award(1, "Best in Show Award", ""));
        awards.save(new Award(2, "The Ed Fair Award", ""));
        exhibits.save(new Exhibit(
                1,
                "GERTIE",
                "Gertie",
                "Boston",
                "MA",
                "Gertie the AS/400",
                "An exhibit about Gertie the AS/400.",
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
        pages.save(new Lrn400Page(
                "LRN400STR",
                1,
                "Welcome to LEARN/400.",
                ""));
        pages.save(new Lrn400Page("LRN400STR", 2, "Two", ""));
        pages.save(new Lrn400Page("LRN400STR", 3, "Three", ""));
        pages.save(new Lrn400Page("LRN400STR", 4, "JUMP", "0006"));
        pages.save(new Lrn400Page("LRN400STR", 6, "Goodbye.", "END"));
    }
}
