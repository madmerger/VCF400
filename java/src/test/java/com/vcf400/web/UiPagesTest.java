package com.vcf400.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.vcf400.domain.GuestbookComment;
import com.vcf400.domain.Vote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@AutoConfigureMockMvc
class UiPagesTest extends AbstractWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void seedComment() {
        comments.save(new GuestbookComment(
                9001,
                "Y",
                "GERTIE",
                "Alice",
                "Hello Gertie"));
    }

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
        void rendersMenu() throws Exception {
            String body = mockMvc.perform(get("/menu?profile=GERTIE"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(body).contains("Gertie the AS/400");
        }

        @Test
        @DisplayName("UI: NTRSTITヘルプを表示する")
        void rendersHelp() throws Exception {
            String body = mockMvc.perform(get("/help"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(body).contains("NTRSTIT");
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
        void rendersReadCommentOutput() throws Exception {
            String body = mockMvc.perform(post("/guestbook/read")
                            .param("profile", "GERTIE")
                            .param("commentId", "9001"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(body).contains(
                    "Currently hosting",
                    "Alice",
                    "Gertie the AS/400",
                    "Hello Gertie");
        }

        @Test
        @DisplayName("UI: LEARN400画面を表示する")
        void rendersLearnContent() throws Exception {
            MvcResult initial = mockMvc.perform(get("/learn400?owner=LRN400STR"))
                    .andExpect(status().isOk())
                    .andReturn();
            MockHttpSession session =
                    (MockHttpSession) initial.getRequest().getSession(false);

            assertThat(initial.getResponse().getContentAsString())
                    .contains("Page", "1", "Welcome to LEARN/400.");

            mockMvc.perform(post("/learn400")
                            .session(session)
                            .param("owner", "LRN400STR")
                            .param("action", "fwd"))
                    .andExpect(status().isOk());
            mockMvc.perform(post("/learn400")
                            .session(session)
                            .param("owner", "LRN400STR")
                            .param("action", "fwd"))
                    .andExpect(status().isOk());
            String finalPage = mockMvc.perform(post("/learn400")
                            .session(session)
                            .param("owner", "LRN400STR")
                            .param("action", "fwd"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(finalPage).contains("Goodbye.");
        }

        @Test
        @DisplayName("BR-LRN400-06/UI: F3 Exitはメニューへリダイレクトする")
        void exitsLearnToHome() throws Exception {
            MvcResult initial = mockMvc.perform(get("/learn400?owner=LRN400STR"))
                    .andExpect(status().isOk())
                    .andReturn();
            MockHttpSession session =
                    (MockHttpSession) initial.getRequest().getSession(false);

            mockMvc.perform(post("/learn400")
                            .session(session)
                            .param("owner", "LRN400STR")
                            .param("action", "exit"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(header().string("Location", "/"));
        }

        @Test
        @DisplayName("UI: LEARN400自動画面を表示する")
        void rendersAutoLearnContent() throws Exception {
            String body = mockMvc.perform(get("/learn400/auto?owner=LRN400STR"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(body).contains("Welcome to LEARN/400.");
        }

        @Test
        @DisplayName("UI: ADMMAINを表示する")
        void rendersAdmin() {
            assertOk("/admin");
        }

        @Test
        @DisplayName("UI: VOTERESを表示する")
        void rendersAdminVotes() throws Exception {
            votes.save(new Vote(9202, 1, "GERTIE"));

            String body = mockMvc.perform(post("/admin/votes")
                            .param("profile", "GERTIE"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(body).contains(
                    "Gertie the AS/400",
                    "Best in Show votes",
                    "Votes in DB",
                    ">1<");
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

        @Test
        @DisplayName("BR-ADDVOTE-05/UI: 成功メッセージをsuccessクラスで表示する")
        void displaysVoteSuccess() throws Exception {
            String body = mockMvc.perform(post("/vote")
                            .param("profile", "GERTIE")
                            .param("badge", "9201")
                            .param("exhibitId", "GERTIE")
                            .param("award", "1"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(body).contains(
                    "class=\"success\"",
                    "You have voted successfully");
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
