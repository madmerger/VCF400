package com.vcf400.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** サインオン無効 (既定) 時: POST /signon は 403、メニューにサインオンフォームを出さない。 */
@SpringBootTest(properties = "vcf.signon.enabled=false")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SignonDisabledTest {
    @Autowired MockMvc mvc;

    @Test
    void signonPostIsRejected() throws Exception {
        mvc.perform(post("/signon").param("profile", "MM2024")).andExpect(status().isForbidden());
    }

    @Test
    void menuHidesSignonForm() throws Exception {
        String html = mvc.perform(get("/menu")).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        Assertions.assertThat(html).doesNotContain("id=\"profile\"").doesNotContain("/signon");
    }
}
