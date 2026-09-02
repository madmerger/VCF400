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
        awards.save(new Award(3, "Peoples Choice Award", ""));
        exhibits.save(new Exhibit(
                1,
                "ASHIBATA",
                "Akira Shibata",
                "Boston",
                "MA",
                "IBM i on PUB400 Demo",
                "An IBM i exhibit running on PUB400.",
                1,
                1));
        exhibits.save(new Exhibit(
                2,
                "DEMO400",
                "Demo 400",
                "Austin",
                "TX",
                "AS/400 Model 150",
                "An AS/400 Model 150 exhibit.",
                1,
                0));
        exhibits.save(new Exhibit(
                3,
                "NOVOTE",
                "No Vote",
                "Boston",
                "MA",
                "No vote title",
                "No vote description",
                0,
                0));
        settings.save(new Setting("ALWVOTE", "Y"));
        settings.save(new Setting("PASSWORD", "VCF2024"));
        pages.save(new Lrn400Page(
                "LRN400STR",
                1,
                "Welcome to LEARN/400! This is page 1. Press F5 for next page, F8 for previous, F3 to exit.",
                ""));
        pages.save(new Lrn400Page(
                "LRN400STR",
                2,
                "The AS/400 was introduced by IBM in June 1988 and became a popular business computer platform. This is page 2.",
                ""));
        pages.save(new Lrn400Page(
                "LRN400STR",
                3,
                "This is the last page. Thank you for visiting VCF/400.",
                "END"));
    }
}
