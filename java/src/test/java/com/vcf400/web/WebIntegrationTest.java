package com.vcf400.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.LinkedHashMap;

import com.vcf400.domain.Award;
import com.vcf400.domain.AwardRepository;
import com.vcf400.domain.Exhibit;
import com.vcf400.domain.ExhibitRepository;
import com.vcf400.domain.Lrn400Page;
import com.vcf400.domain.Lrn400PageRepository;
import com.vcf400.domain.Setting;
import com.vcf400.domain.SettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private AwardRepository awards;

    @Autowired
    private ExhibitRepository exhibits;

    @Autowired
    private SettingRepository settings;

    @Autowired
    private Lrn400PageRepository pages;

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
    @DisplayName("BR-ADDVOTE-05/API: 成功投票から重複拒否まで")
    void voteApiScenario() {
        Map<String, Object> body = Map.of(
                "badge", 1001,
                "exhibitId", "GERTIE",
                "award", 1,
                "profile", "GERTIE");

        ResponseEntity<Map> first =
                rest.postForEntity("/api/vote", body, Map.class);
        ResponseEntity<Map> duplicate =
                rest.postForEntity("/api/vote", body, Map.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(first.getBody()).containsEntry("success", true);
        assertThat(duplicate.getBody())
                .containsEntry("errLine", "You have already voted.");
    }

    @Test
    @DisplayName("BR-ADMCRTEXHB-04/API: 既存出展の新規登録エラー")
    void existingExhibitApiScenario() {
        Map<String, Object> body = Map.of(
                "profile", "GERTIE",
                "owner", "Duplicate",
                "city", "Boston",
                "state", "MA",
                "title", "Duplicate",
                "description", "Duplicate",
                "eligible", 1,
                "learn400", 1);

        ResponseEntity<Map> response =
                rest.postForEntity("/api/admin/exhibits", body, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .containsEntry("message", "Error - USRPRF exists");
    }

    @Test
    @DisplayName("BR-ADDVOTE-02/UI: 投票フォームのERRLINE表示")
    void voteUiDisplaysErrorLine() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("profile", "MM2024");
        form.add("badge", "0");
        form.add("exhibitId", "");
        form.add("award", "0");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(form, headers);
        ResponseEntity<String> response = rest.exchange(
                "/vote",
                HttpMethod.POST,
                request,
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Must enter Exhibit ID");
    }

    @Test
    @DisplayName("UI: 代表画面のHTTP応答")
    void uiPathsRender() {
        String[] paths = {
                "/",
                "/menu?profile=GERTIE",
                "/help",
                "/vote?profile=GERTIE",
                "/guestbook/add?profile=GERTIE",
                "/guestbook/read?profile=GERTIE",
                "/learn400?owner=LRN400STR",
                "/learn400/auto",
                "/admin",
                "/admin/votes",
                "/admin/exhibits",
                "/admin/comments",
                "/admin/learn400",
                "/admin/officers",
                "/admin/settings",
                "/admin/beemovie",
                "/credits",
                "/admin/spool"
        };

        for (String path : paths) {
            ResponseEntity<String> response = rest.getForEntity(path, String.class);
            assertThat(response.getStatusCode())
                    .as(path)
                    .isEqualTo(HttpStatus.OK);
        }
    }
}
