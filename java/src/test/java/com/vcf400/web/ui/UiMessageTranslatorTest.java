package com.vcf400.web.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.vcf400.service.RpgMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

class UiMessageTranslatorTest {

    private UiMessageTranslator translator;

    @BeforeEach
    void setUp() {
        ReloadableResourceBundleMessageSource messages =
                new ReloadableResourceBundleMessageSource();
        messages.setBasename("classpath:messages");
        messages.setDefaultEncoding("UTF-8");
        translator = new UiMessageTranslator(messages);
    }

    @Test
    void translatesKnownRpgMessage() {
        assertThat(translator.tr(RpgMessages.EXISTS))
                .isEqualTo("すでに投票済みです。");
    }

    @Test
    void leavesUnknownMessageUnchanged() {
        assertThat(translator.tr("not a configured RPG message"))
                .isEqualTo("not a configured RPG message");
    }

    @Test
    void trimsTrailingWhitespaceBeforeTranslation() {
        assertThat(translator.tr(RpgMessages.NO_NAME))
                .isEqualTo("お名前を入力してください");
    }
}
