package com.vcf400.web;

import com.vcf400.service.Messages;
import static org.hamcrest.Matchers.containsString;
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

    @Test
    void mainMenuKeepsLegacyTitleAndNumbers() throws Exception {
        mvc.perform(get("/menu")).andExpect(status().isOk())
            .andExpect(content().string(containsString("AS/400 DEMO MENU")))
            .andExpect(content().string(containsString("data-option=\"11\"")))
            .andExpect(content().string(containsString("data-option=\"80\"")));
    }

    @Test
    void menuNumbersRouteLikeVcfmain() throws Exception {
        mvc.perform(post("/menu").param("option", "1")).andExpect(redirectedUrl("/learn"));
        mvc.perform(post("/menu").param("option", "11")).andExpect(redirectedUrl("/navigate?next=/vote"));
        mvc.perform(post("/menu").param("option", "12")).andExpect(redirectedUrl("/navigate?next=/guestbook/add"));
        mvc.perform(post("/menu").param("option", "13")).andExpect(redirectedUrl("/navigate?next=/guestbook/read"));
        mvc.perform(post("/menu").param("option", "80")).andExpect(redirectedUrl("/signoff"));
    }

    @Test
    void longMenuNumberIsInvalidInsteadOfOverflowing() throws Exception {
        mvc.perform(post("/menu").param("option", "99999999999"))
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
        mvc.perform(post("/vote").param("inputBadge", "").param("inExhb", "ASHIBATA").param("inputAward", "1"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(Messages.ERRBLKBG)));
    }

    @Test
    void cancelReturnsToCaller() throws Exception {
        mvc.perform(post("/vote").param("action", "cancel")).andExpect(redirectedUrl("/menu"));
        MockHttpSession kioskSession = new MockHttpSession();
        mvc.perform(get("/kiosk/ASHIBATA").session(kioskSession)).andExpect(status().isOk())
            .andExpect(content().string(containsString("WELCOME TO...")));
        mvc.perform(post("/guestbook/add").param("action", "cancel").session(kioskSession))
            .andExpect(redirectedUrl("/kiosk/ASHIBATA"));
    }

    @Test
    void kioskHidesOptionsByFlagsAndOption7AsksPassword() throws Exception {
        String novote = mvc.perform(get("/kiosk/NOVOTE")).andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(novote).doesNotContain("data-option=\"1\"").doesNotContain("data-option=\"2\"");
        String ashibata = mvc.perform(get("/kiosk/ASHIBATA")).andReturn().getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(ashibata).contains("data-option=\"1\"", "data-option=\"2\"");
        org.assertj.core.api.Assertions.assertThat(ashibata).doesNotContain("data-option=\"7\"");   // 隠しオプション
        MockHttpSession session = new MockHttpSession();
        mvc.perform(get("/kiosk/ASHIBATA").session(session)).andExpect(status().isOk());
        mvc.perform(post("/kiosk/ASHIBATA").param("option", "7").session(session))
            .andExpect(redirectedUrl("/kiosk/ASHIBATA/exit"));
        mvc.perform(post("/kiosk/ASHIBATA/exit").param("inPwd", "wrong").session(session))
            .andExpect(redirectedUrl("/kiosk/ASHIBATA"));
        mvc.perform(post("/kiosk/ASHIBATA/exit").param("inPwd", "VCF2024").session(session))
            .andExpect(redirectedUrl("/menu?msg=" + java.net.URLEncoder.encode(
                    Messages.MSG_KIOSK_ENDED.formatted("ASHIBATA"), java.nio.charset.StandardCharsets.UTF_8)));
    }

    @Test
    void kioskExitWithoutLaunchReturnsToKiosk() throws Exception {
        mvc.perform(post("/kiosk/ASHIBATA/exit").param("inPwd", "VCF2024"))
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
        MockHttpSession s = new MockHttpSession();
        mvc.perform(get("/learn").session(s)).andExpect(status().isOk())
            .andExpect(content().string(containsString("LEARN/400")))
            .andExpect(content().string(containsString("F3")))
            .andExpect(content().string(containsString("F8")));
        mvc.perform(post("/learn").param("action", "fwd").session(s)).andExpect(status().isOk());
        mvc.perform(post("/learn").param("action", "fwd").session(s)).andExpect(redirectedUrl("/menu"));  // EXTRA='END'
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
    void csrfRejectsCrossSiteOriginAndFetchSite() throws Exception {
        mvc.perform(post("/vote").param("inputBadge", "1")
                .header("Origin", "http://evil.example"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/vote").param("inputBadge", "1")
                .header("Sec-Fetch-Site", "cross-site"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/vote").param("inputBadge", "1")
                .header("Origin", "http://localhost"))
            .andExpect(status().isOk());
    }

    @Test
    void kioskOption2ShowsExhibitLearnPages() throws Exception {
        jdbc.update("INSERT INTO LRN400STR (LRNFILE, PAGENBR, CONTENT, EXTRA) " +
                "VALUES ('ASHIBATA', 1, 'ASHIBATA kiosk learn page', 'END')");
        try {
            MockHttpSession session = new MockHttpSession();
            mvc.perform(post("/kiosk/ASHIBATA").param("option", "2").session(session))
                .andExpect(redirectedUrl("/learn"));
            mvc.perform(get("/learn").session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("ASHIBATA kiosk learn page")));
            // VCFMAIN 1 (セッション属性なし) は共通 LRN400STR のまま
            MockHttpSession plain = new MockHttpSession();
            mvc.perform(get("/learn").session(plain)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Welcome to LEARN/400")));
        } finally {
            jdbc.update("DELETE FROM LRN400STR WHERE LRNFILE = 'ASHIBATA'");
        }
    }

    @Test
    void guestbookReadRequiresId() throws Exception {
        mvc.perform(post("/guestbook/read").param("inCmtId", ""))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(Messages.ERRCMTID)));
        mvc.perform(post("/guestbook/read").param("inCmtId", "1"))
            .andExpect(content().string(containsString("GUESTBOOK/400 - Read a Comment")))
            .andExpect(content().string(containsString("さんから、宛先:")));
    }
}
