package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StaticScreenServiceTest extends AbstractRpgServiceTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-CREDITS-01: CREDITS固定文言を返す")
        void returnsCredits() {
            assertThat(staticScreens.credits().get("text")).contains("VCF/400");
        }

        @Test
        @DisplayName("BR-NTRSTIT-01: ヘルプ画面にRIGHT CTRLを表示する")
        void returnsHelp() {
            assertThat(staticScreens.help().get("text")).contains("RIGHT CTRL");
        }

        @Test
        @DisplayName("BR-PARAMETER-01: PARAMETERのfirstを返す")
        void returnsFirstParameter() {
            assertThat(staticScreens.parameters("first", "second").get("first"))
                    .isEqualTo("first");
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-PARAMETER-01: nullパラメータを許容する")
        void acceptsNullParameters() {
            assertThat(staticScreens.parameters(null, null)).containsKeys("first", "second");
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-PARAMETER-01: firstはDDS桁20で切り捨てる")
        void truncatesFirstParameter() {
            assertThat(staticScreens.parameters(
                    "123456789012345678901",
                    "second").get("first")).hasSize(20);
        }

        @Test
        @DisplayName("BR-PARAMETER-01: secondはDDS桁20で切り捨てる")
        void truncatesSecondParameter() {
            assertThat(staticScreens.parameters(
                    "first",
                    "123456789012345678901").get("second")).hasSize(20);
        }
    }
}
