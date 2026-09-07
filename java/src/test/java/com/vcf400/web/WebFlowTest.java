package com.vcf400.web;

import com.vcf400.service.Messages;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

/** 画面遷移 (L-01..L-10) と見出し文言の保持を確認する。 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebFlowTest {
    @Autowired MockMvc mvc;

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
        mvc.perform(post("/kiosk/ASHIBATA").param("option", "7")).andExpect(redirectedUrl("/kiosk/ASHIBATA/exit"));
        mvc.perform(post("/kiosk/ASHIBATA/exit").param("inPwd", "wrong")).andExpect(redirectedUrl("/kiosk/ASHIBATA"));
        mvc.perform(post("/kiosk/ASHIBATA/exit").param("inPwd", "VCF2024"))
            .andExpect(redirectedUrl("/menu?msg=" + java.net.URLEncoder.encode(
                    Messages.MSG_KIOSK_ENDED.formatted("ASHIBATA"), java.nio.charset.StandardCharsets.UTF_8)));
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
    void guestbookReadRequiresId() throws Exception {
        mvc.perform(post("/guestbook/read").param("inCmtId", ""))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString(Messages.ERRCMTID)));
        mvc.perform(post("/guestbook/read").param("inCmtId", "1"))
            .andExpect(content().string(containsString("GUESTBOOK/400 - Read a Comment")))
            .andExpect(content().string(containsString("さんから、宛先:")));
    }
}
