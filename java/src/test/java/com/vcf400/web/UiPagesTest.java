package com.vcf400.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

class UiPagesTest extends AbstractWebIntegrationTest {

    private void assertOk(String path) {
        assertThat(rest.getForEntity(path, String.class).getStatusCode())
                .as(path)
                .isEqualTo(HttpStatus.OK);
    }

    @Nested
    class Normal {

        @Test
        @DisplayName("UI: VCFMAINホームを表示する")
        void rendersHome() {
            assertOk("/");
        }

        @Test
        @DisplayName("UI: EXHBMENUを表示する")
        void rendersMenu() {
            assertOk("/menu?profile=GERTIE");
        }

        @Test
        @DisplayName("UI: NTRSTITヘルプを表示する")
        void rendersHelp() {
            assertOk("/help");
        }

        @Test
        @DisplayName("UI: VOTE1画面を表示する")
        void rendersVote() {
            assertOk("/vote?profile=GERTIE");
        }

        @Test
        @DisplayName("UI: ADDCMT画面を表示する")
        void rendersAddComment() {
            assertOk("/guestbook/add?profile=GERTIE");
        }

        @Test
        @DisplayName("UI: READCMT画面を表示する")
        void rendersReadComment() {
            assertOk("/guestbook/read?profile=GERTIE");
        }

        @Test
        @DisplayName("UI: LEARN400画面を表示する")
        void rendersLearn() {
            assertOk("/learn400?owner=LRN400STR");
        }

        @Test
        @DisplayName("UI: LEARN400自動画面を表示する")
        void rendersAutoLearn() {
            assertOk("/learn400/auto");
        }

        @Test
        @DisplayName("UI: ADMMAINを表示する")
        void rendersAdmin() {
            assertOk("/admin");
        }

        @Test
        @DisplayName("UI: VOTERESを表示する")
        void rendersAdminVotes() {
            assertOk("/admin/votes");
        }

        @Test
        @DisplayName("UI: ADMCREATEを表示する")
        void rendersAdminExhibits() {
            assertOk("/admin/exhibits");
        }

        @Test
        @DisplayName("UI: HIDECMTを表示する")
        void rendersAdminComments() {
            assertOk("/admin/comments");
        }

        @Test
        @DisplayName("UI: MAINADMINを表示する")
        void rendersAdminLearn() {
            assertOk("/admin/learn400");
        }

        @Test
        @DisplayName("UI: ADDADMを表示する")
        void rendersAdminOfficers() {
            assertOk("/admin/officers");
        }

        @Test
        @DisplayName("UI: SETUPMAINを表示する")
        void rendersAdminSettings() {
            assertOk("/admin/settings");
        }

        @Test
        @DisplayName("UI: BEEMOVIEを表示する")
        void rendersBeeMovie() {
            assertOk("/admin/beemovie");
        }

        @Test
        @DisplayName("UI: CREDITSを表示する")
        void rendersCredits() {
            assertOk("/credits");
        }

        @Test
        @DisplayName("UI: PARAMETERを表示する")
        void rendersParameters() {
            assertOk("/parameter");
        }

        @Test
        @DisplayName("UI: 印刷スプール一覧を表示する")
        void rendersSpool() {
            assertOk("/admin/spool");
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-ADDVOTE-02/UI: 投票フォームにERRLINEを表示する")
        void displaysVoteError() {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("profile", "MM2024");
            form.add("badge", "0");
            form.add("exhibitId", "");
            form.add("award", "0");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            ResponseEntity<String> response = rest.exchange(
                    "/vote",
                    HttpMethod.POST,
                    new HttpEntity<>(form, headers),
                    String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).contains("Must enter Exhibit ID");
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("UI: ヘルプ省略クエリ付き投票画面を表示する")
        void rendersVoteWithoutHelp() {
            assertOk("/vote?profile=GERTIE&help=1");
        }
    }
}
