package com.vcf400.web;

import com.vcf400.service.Messages;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/** 画面遷移 (L-01..L-10) と見出し文言の保持を確認する。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebFlowTest {
    @Autowired MockMvc mvc;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;

    private static MockHttpSession csrfSession() {
        MockHttpSession s = new MockHttpSession();
        s.setAttribute(com.vcf400.config.CsrfFilter.SESSION_KEY, "tok");
        return s;
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder csrf(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder b,
            MockHttpSession session) {
        return b.session(session).param("_csrf", "tok");
    }

    @Test
    void mainMenuKeepsLegacyTitleAndNumbers() throws Exception {
        mvc.perform(get("/menu")).andExpect(status().isOk())
            .andExpect(content().string(containsString("AS/400 DEMO MENU")))
            .andExpect(content().string(containsString("data-option=\"11\"")))
            .andExpect(content().string(containsString("data-option=\"80\"")));
    }

    @Test
    void menuNumbersRouteLikeVcfmain() throws Exception {
        mvc.perform(csrf(post("/menu"), csrfSession()).param("option", "1")).andExpect(redirectedUrl("/learn"));
        mvc.perform(csrf(post("/menu"), csrfSession()).param("option", "11")).andExpect(redirectedUrl("/navigate?next=/vote"));
        mvc.perform(csrf(post("/menu"), csrfSession()).param("option", "12")).andExpect(redirectedUrl("/navigate?next=/guestbook/add"));
        mvc.perform(csrf(post("/menu"), csrfSession()).param("option", "13")).andExpect(redirectedUrl("/navigate?next=/guestbook/read"));
        mvc.perform(csrf(post("/menu"), csrfSession()).param("option", "80")).andExpect(redirectedUrl("/signoff"));
    }

    @Test
    void longMenuNumberIsInvalidInsteadOfOverflowing() throws Exception {
        mvc.perform(csrf(post("/menu"), csrfSession()).param("option", "99999999999"))
            .andExpect(redirectedUrl("/menu?msg=" + java.net.URLEncoder.encode(
                    Messages.MSG_MENU_INVALID, java.nio.charset.StandardCharsets.UTF_8)));
    }

    @Test
    void navigateRejectsProtocolRelativeNext() throws Exception {
        mvc.perform(get("/navigate").param("next", "//evil.com"))
            .andExpect(model().attribute("next", "/menu"));
    }

    @Test
    void voteScreenKeepsTitleFieldOrderAndKeys() throws Exception {
        String html = mvc.perform(get("/vote")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        int badge = html.indexOf("name=\"inputBadge\"");
        int exhb = html.indexOf("name=\"inExhb\"");
        int award = html.indexOf("name=\"inputAward\"");
        org.assertj.core.api.Assertions.assertThat(html).contains("NOMINATE EXHIBIT FOR AWARD", "F5", "F12", "今年のアワード一覧:");
        org.assertj.core.api.Assertions.assertThat(badge).isLessThan(exhb);
        org.assertj.core.api.Assertions.assertThat(exhb).isLessThan(award);
    }

    @Test
    void voteValidationErrorIsShownOnSameScreen() throws Exception {
        mvc.perform(csrf(post("/vote"), csrfSession()).param("inputBadge", "").param("inExhb", "ASHIBATA").param("inputAward", "1"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(Messages.ERRBLKBG)));
    }

    @Test
    void cancelReturnsToCaller() throws Exception {
        mvc.perform(csrf(post("/vote"), csrfSession()).param("action", "cancel")).andExpect(redirectedUrl("/menu"));
        MockHttpSession kioskSession = csrfSession();
        mvc.perform(get("/kiosk/ASHIBATA").session(kioskSession)).andExpect(status().isOk())
            .andExpect(content().string(containsString("WELCOME TO...")));
        mvc.perform(csrf(post("/guestbook/add"), kioskSession).param("action", "cancel"))
            .andExpect(redirectedUrl("/kiosk/ASHIBATA"));
    }

    @Test
    void kioskHidesOptionsByFlagsAndOption7AsksPassword() throws Exception {
        String novote = mvc.perform(get("/kiosk/NOVOTE")).andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(novote).doesNotContain("data-option=\"1\"").doesNotContain("data-option=\"2\"");
        String ashibata = mvc.perform(get("/kiosk/ASHIBATA")).andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(ashibata).contains("data-option=\"1\"", "data-option=\"2\"");
        org.assertj.core.api.Assertions.assertThat(ashibata).doesNotContain("data-option=\"7\"");   // 隠しオプション
        MockHttpSession session = csrfSession();
        mvc.perform(get("/kiosk/ASHIBATA").session(session)).andExpect(status().isOk());
        mvc.perform(csrf(post("/kiosk/ASHIBATA"), session).param("option", "7"))
            .andExpect(redirectedUrl("/kiosk/ASHIBATA/exit"));
        mvc.perform(csrf(post("/kiosk/ASHIBATA/exit"), session).param("inPwd", "wrong"))
            .andExpect(redirectedUrl("/kiosk/ASHIBATA"));
        mvc.perform(csrf(post("/kiosk/ASHIBATA/exit"), session).param("inPwd", "VCF2024"))
            .andExpect(redirectedUrl("/menu?msg=" + java.net.URLEncoder.encode(
                    Messages.MSG_KIOSK_ENDED.formatted("ASHIBATA"), java.nio.charset.StandardCharsets.UTF_8)));
    }

    @Test
    void kioskExitWithoutLaunchReturnsToKiosk() throws Exception {
        mvc.perform(csrf(post("/kiosk/ASHIBATA/exit"), csrfSession()).param("inPwd", "VCF2024"))
            .andExpect(redirectedUrl("/kiosk/ASHIBATA"));
    }

    @Test
    void dbApiRejectsNonLoopbackRequests() throws Exception {
        RequestPostProcessor remote = request -> {
            request.setRemoteAddr("10.0.0.1");
            return request;
        };
        mvc.perform(get("/api/db/votes").with(remote)).andExpect(status().isForbidden());
    }

    @Test
    void learnScreenHasF3F5F8() throws Exception {
        MockHttpSession s = csrfSession();
        mvc.perform(get("/learn").session(s)).andExpect(status().isOk())
            .andExpect(content().string(containsString("LEARN/400")))
            .andExpect(content().string(containsString("F3")))
            .andExpect(content().string(containsString("F8")));
        mvc.perform(csrf(post("/learn"), s).param("action", "fwd")).andExpect(status().isOk());
        mvc.perform(csrf(post("/learn"), s).param("action", "fwd")).andExpect(redirectedUrl("/menu"));  // EXTRA='END'
        HttpSession after = s;
        org.assertj.core.api.Assertions.assertThat(after.getAttribute("vcf.learnState")).isNull();
    }

    @Test
    void dbApiMasksAdmpswrdAndWhitelistsSettingNames() throws Exception {
        String json = mvc.perform(get("/api/db/settings")).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(json).contains("********").doesNotContain("VCF2024");
        mvc.perform(put("/api/db/settings/ALWVOTE").contentType("application/json")
                .content("{\"value\":\"Y\"}")).andExpect(status().isOk());
        mvc.perform(put("/api/db/settings/ADMPSWRD").contentType("application/json")
                .content("{\"value\":\"VCF2024\"}")).andExpect(status().isOk());
        mvc.perform(put("/api/db/settings/PASSWORD").contentType("application/json")
                .content("{\"value\":\"x\"}")).andExpect(status().isBadRequest());
    }

    @Test
    void csrfRequiresSessionToken() throws Exception {
        // トークンなし / セッションなし / 不一致は 403、正しいトークンで通る
        mvc.perform(post("/vote").param("inputBadge", "1")).andExpect(status().isForbidden());
        mvc.perform(post("/vote").session(csrfSession()).param("inputBadge", "1").param("_csrf", "wrong"))
            .andExpect(status().isForbidden());
        MockHttpSession s = csrfSession();
        mvc.perform(csrf(post("/vote"), s).param("inputBadge", "1")).andExpect(status().isOk());
        // GET で配られるフォームに hidden _csrf が含まれる
        mvc.perform(get("/menu")).andExpect(status().isOk())
            .andExpect(content().string(containsString("name=\"_csrf\"")));
        // /api/db/ はトークン不要 (フラグ + ループバックで保護)
        mvc.perform(put("/api/db/settings/ALWVOTE").contentType("application/json")
                .content("{\"value\":\"Y\"}")).andExpect(status().isOk());
        // ブラウザ起点の /api/db 変更は拒否 (Origin / Sec-Fetch-Site があれば 403、JSON 以外も 403)
        mvc.perform(put("/api/db/settings/ALWVOTE").contentType("application/json")
                .header("Origin", "http://evil.example").content("{\"value\":\"N\"}"))
            .andExpect(status().isForbidden());
        mvc.perform(put("/api/db/settings/ALWVOTE").contentType("application/json")
                .header("Sec-Fetch-Site", "cross-site").content("{\"value\":\"N\"}"))
            .andExpect(status().isForbidden());
        mvc.perform(put("/api/db/settings/ALWVOTE").content("value=N"))
            .andExpect(status().isForbidden());
        // ボディなし DELETE は content-type 不要だが、Origin / フォーム CT は拒否
        mvc.perform(delete("/api/db/votes/7001")).andExpect(status().isOk());
        mvc.perform(delete("/api/db/votes/7001").header("Origin", "http://evil.example"))
            .andExpect(status().isForbidden());
        mvc.perform(delete("/api/db/votes/7001").contentType("application/x-www-form-urlencoded"))
            .andExpect(status().isForbidden());
    }

    @Test
    void kioskOption2ShowsExhibitLearnPages() throws Exception {
        jdbc.update("INSERT INTO LRN400STR (LRNFILE, PAGENBR, CONTENT, EXTRA) " +
                "VALUES ('ASHIBATA', 1, 'ASHIBATA kiosk learn page', 'END')");
        try {
            MockHttpSession session = csrfSession();
            mvc.perform(csrf(post("/kiosk/ASHIBATA"), session).param("option", "2"))
                .andExpect(redirectedUrl("/learn?exhb=ASHIBATA"));
            mvc.perform(get("/learn").param("exhb", "ASHIBATA").session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("ASHIBATA kiosk learn page")));
            // VCFMAIN 1 (パラメータなし) と ENLRN400=0 / 存在しない展示は共通 LRN400STR のまま
            mvc.perform(get("/learn")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome to LEARN/400")));
            mvc.perform(get("/learn").param("exhb", "DEMO400")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome to LEARN/400")));
            mvc.perform(get("/learn").param("exhb", "NOSUCH")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome to LEARN/400")));
        } finally {
            jdbc.update("DELETE FROM LRN400STR WHERE LRNFILE = 'ASHIBATA'");
        }
    }

    @Test
    void kioskOption2EncodesExhibitIdWithSpecialChars() throws Exception {
        // 展示 ID に '#' が含まれても RedirectAttributes で URL エンコードされ /learn へ届く
        jdbc.update("INSERT INTO EXHBDB (EXHBDBID, EXHUSRPRF, EXHBITOR, ELIGIBLE, ENLRN400) " +
                "VALUES (99, 'EX#A', 'Hash Exhibitor', 1, 1)");
        jdbc.update("INSERT INTO LRN400STR (LRNFILE, PAGENBR, CONTENT, EXTRA) " +
                "VALUES ('EX#A', 1, 'hash exhibit learn page', 'END')");
        try {
            mvc.perform(csrf(post("/kiosk/{exhibit}", "EX#A"), csrfSession()).param("option", "2"))
                .andExpect(redirectedUrl("/learn?exhb=EX%23A"));
            mvc.perform(get("/learn").param("exhb", "EX#A")).andExpect(status().isOk())
                .andExpect(content().string(containsString("hash exhibit learn page")));
        } finally {
            jdbc.update("DELETE FROM LRN400STR WHERE LRNFILE = 'EX#A'");
            jdbc.update("DELETE FROM EXHBDB WHERE EXHUSRPRF = 'EX#A'");
        }
    }

    @Test
    void guestbookReadRequiresId() throws Exception {
        mvc.perform(csrf(post("/guestbook/read"), csrfSession()).param("inCmtId", ""))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(Messages.ERRCMTID)));
        mvc.perform(csrf(post("/guestbook/read"), csrfSession()).param("inCmtId", "1"))
            .andExpect(content().string(containsString("GUESTBOOK/400 - Read a Comment")))
            .andExpect(content().string(containsString("さんから、宛先:")));
    }
}
