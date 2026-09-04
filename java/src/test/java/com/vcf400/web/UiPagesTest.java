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
                2,
                "Y",
                "ASHIBATA",
                "Devin",
                "VCF/400 is running on PUB400."));
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
            String body = rest.getForEntity("/", String.class).getBody();
            assertThat(body).contains(
                    "ASHIBATA",
                    "DEMO400",
                    "NOVOTE",
                    "MM2024",
                    "1 LEARN/400",
                    "11 展示を賞に推薦(投票)",
                    "80 展示メニュー");
        }

        @Test
        @DisplayName("UI: VCFMAINのLEARN/400はLRN400STRへ遷移する")
        void redirectsLearnOptionToGenericLearn() throws Exception {
            mockMvc.perform(post("/")
                            .param("profile", "ASHIBATA")
                            .param("option", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(header().string(
                            "Location",
                            "/learn400?owner=LRN400STR"));
        }

        @Test
        @DisplayName("UI: EXHBMENUを表示する")
        void rendersMenu() throws Exception {
            String body = mockMvc.perform(get("/menu?profile=ASHIBATA"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(body).contains("IBM i on PUB400 Demo");
        }

        @Test
        @DisplayName("UI: NTRSTITヘルプを表示する")
        void rendersHelp() throws Exception {
            String body = mockMvc.perform(get("/help"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(body).contains("NTRSTIT", "右Ctrl(=Enter)で続行してください。");
        }

        @Test
        @DisplayName("UI: VOTE1画面を表示する")
        void rendersVote() {
            String body = rest.getForEntity("/vote?profile=ASHIBATA", String.class).getBody();
            assertThat(body).contains(
                    "ヴィンテージ・コンピュータ・フェスティバル  展示を賞に推薦",
                    "readonly");
        }

        @Test
        @DisplayName("UI: ADDCMT画面を表示する")
        void rendersAddComment() {
            String body = rest.getForEntity("/guestbook/add?profile=ASHIBATA", String.class).getBody();
            assertThat(body).contains(
                    "ヴィンテージ・コンピュータ・フェスティバル  GUESTBOOK/400 - コメントを追加",
                    "maxlength=\"20\"",
                    "maxlength=\"200\"");
        }

        @Test
        @DisplayName("UI: READCMT画面を表示する")
        void rendersReadCommentOutput() throws Exception {
            String body = mockMvc.perform(post("/guestbook/read")
                            .param("profile", "ASHIBATA")
                            .param("commentId", "2"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(body).contains(
                    "現在 0002 件のコメントを掲載中です。",
                    "Devin",
                    "IBM i on PUB400 Demo",
                    "VCF/400 is running on PUB400.");
        }

        @Test
        @DisplayName("UI: READCMT初期表示は最大コメントIDを4桁表示する")
        void rendersInitialReadCommentCount() throws Exception {
            comments.deleteAll();
            comments.save(new GuestbookComment(
                    1,
                    "Y",
                    "ASHIBATA",
                    "Devin",
                    "VCF/400 is running on PUB400."));

            String body = mockMvc.perform(get("/guestbook/read?profile=ASHIBATA"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(body).contains("現在 0001 件のコメントを掲載中です。");
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
                    .contains("ページ", "0001", "Welcome to LEARN/400! This is page 1.");

            String firstPage = mockMvc.perform(post("/learn400")
                            .session(session)
                            .param("owner", "LRN400STR")
                            .param("action", "fwd"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            assertThat(firstPage).contains("ページ", "0002", "This is page 2");

            String secondPage = mockMvc.perform(post("/learn400")
                            .session(session)
                            .param("owner", "LRN400STR")
                            .param("action", "fwd"))
                            .andExpect(status().isOk())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

            assertThat(secondPage).contains("ページ", "0003", "This is the last page.");

            assertThat(secondPage).contains(
                    "ページ",
                    "0003",
                    "This is the last page.",
                    "END");
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

            assertThat(body).contains(
                    "ページ",
                    "0001",
                    "Welcome to LEARN/400! This is page 1.");
        }

        @Test
        @DisplayName("UI: ADMMAINを表示する")
        void rendersAdmin() {
            assertOk("/admin");
        }

        @Test
        @DisplayName("UI: VOTERESを表示する")
        void rendersAdminVotes() throws Exception {
            votes.save(new Vote(9202, 1, "ASHIBATA"));

            String body = mockMvc.perform(post("/admin/votes")
                            .param("profile", "ASHIBATA"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(body).contains(
                    "IBM i on PUB400 Demo",
                    "ベスト・イン・ショー賞の票数",
                    "DB内の投票数",
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

        @Test
        @DisplayName("UI: DB検証画面に投票とコメントの表を表示する")
        void rendersDatabaseContents() {
            votes.save(new Vote(28, 2, "ASHIBATA"));
            String body = rest.getForEntity("/admin/db", String.class).getBody();

            assertThat(body).contains(
                    "VOTINGDB",
                    "BADGENBR",
                    "28",
                    "GUESTBKDB",
                    "VCF/400 is running on PUB400.");
        }

        @Test
        @DisplayName("UI: サインオン画面でユーザーを再選択できる")
        void rendersSignon() {
            String body = rest.getForEntity("/signon", String.class).getBody();
            assertThat(body).contains("VCF/400 サインオン", "ASHIBATA", "MM2024");
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
            assertThat(response.getBody()).contains("展示 ID を入力してください");
        }

        @Test
        @DisplayName("BR-ADDVOTE-05/UI: 成功メッセージをsuccessクラスで表示する")
        void displaysVoteSuccess() throws Exception {
            String body = mockMvc.perform(post("/vote")
                            .param("profile", "ASHIBATA")
                            .param("badge", "9201")
                            .param("exhibitId", "ASHIBATA")
                            .param("award", "1"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(body).contains(
                    "class=\"success\"",
                    "あなたの投票は記録されました!",
                    "Enter キーでメインメニューに戻ります。");
        }

        @Test
        @DisplayName("BR-ADDVOTE-05/UI: 重複投票メッセージを日本語表示する")
        void displaysDuplicateVoteMessageInJapanese() throws Exception {
            mockMvc.perform(post("/vote")
                            .param("profile", "ASHIBATA")
                            .param("badge", "9203")
                            .param("exhibitId", "ASHIBATA")
                            .param("award", "1"))
                    .andExpect(status().isOk());

            String body = mockMvc.perform(post("/vote")
                            .param("profile", "ASHIBATA")
                            .param("badge", "9203")
                            .param("exhibitId", "ASHIBATA")
                            .param("award", "1"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertThat(body).contains("すでに投票済みです。");
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("UI: ヘルプ省略クエリ付き投票画面を表示する")
        void rendersVoteWithoutHelp() {
            assertOk("/vote?profile=ASHIBATA&help=1");
        }
    }
}
