package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AdminSecurityOfficerServiceTest extends AbstractRpgServiceTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-ADMADDSOFR-01: 管理者を追加する")
        void addsOfficer() {
            assertThat(securityOfficer.add("MM2024"))
                    .isEqualTo(RpgMessages.STATUS_UPDATED);
        }

        @Test
        @DisplayName("BR-ADMOFRLIST-01: 管理者一覧を返す")
        void listsOfficers() {
            securityOfficer.add("MM2024");

            assertThat(securityOfficer.list()).hasSize(1);
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-ADMADDSOFR-02: 重複管理者はError - USRPRF exists")
        void rejectsDuplicateOfficer() {
            securityOfficer.add("MM2024");

            assertThat(securityOfficer.add("MM2024"))
                    .isEqualTo(RpgMessages.USER_EXISTS);
        }

        @Test
        @DisplayName("BR-ADMADDSOFR-01: 空profileは必須エラー")
        void rejectsBlankOfficer() {
            assertThat(securityOfficer.add(""))
                    .isEqualTo(RpgMessages.USER_BLANK);
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-ADMADDSOFR-03: profileはDDS桁9で切り捨てる")
        void truncatesOfficerProfile() {
            securityOfficer.add("MM2024XXXX");

            assertThat(securityOfficer.list()).extracting("userProf")
                    .contains("MM2024XXX");
        }
    }
}
