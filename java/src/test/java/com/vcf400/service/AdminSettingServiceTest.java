package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AdminSettingServiceTest extends AbstractRpgServiceTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-ADMSETTING-01: loadはALWVOTE=Yを画面値1へ変換する")
        void loadsAllowVoteOn() {
            assertThat(setting.load().get("ALWVOTE")).isEqualTo("Y");
        }

        @Test
        @DisplayName("BR-ADMSETTING-02: saveの0はALWVOTE=Nへ変換する")
        void savesAllowVoteOff() {
            setting.save("VCF400", "0");

            assertThat(settings.findById("ALWVOTE").orElseThrow().getValue())
                    .isEqualTo("N");
        }

        @Test
        @DisplayName("BR-ADMSETTING-03: PASSWORDを保存する")
        void savesPassword() {
            setting.save("NEWPASS", "1");

            assertThat(settings.findById("PASSWORD").orElseThrow().getValue())
                    .isEqualTo("NEWPASS");
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-ADMSETTING-02: 不正なALWVOTE値はそのまま保持する")
        void preservesInvalidAllowVote() {
            setting.save("VCF400", "X");

            assertThat(settings.findById("ALWVOTE").orElseThrow().getValue())
                    .isEqualTo("X");
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-ADMSETTING-03: PASSWORDはDDS桁9で切り捨てる")
        void truncatesPassword() {
            setting.save("LONGPASSWORD", "1");

            assertThat(settings.findById("PASSWORD").orElseThrow().getValue())
                    .isEqualTo("LONGPASSW");
        }

        @Test
        @DisplayName("BR-ADMSETTING-02: ALWVOTEはDDS桁1で切り捨てる")
        void truncatesAllowVote() {
            setting.save("VCF400", "01");

            assertThat(settings.findById("ALWVOTE").orElseThrow().getValue())
                    .isEqualTo("N");
        }
    }
}
