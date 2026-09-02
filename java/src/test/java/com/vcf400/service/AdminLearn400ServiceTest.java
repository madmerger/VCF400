package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.vcf400.domain.Lrn400Page;
import com.vcf400.domain.Lrn400PageId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AdminLearn400ServiceTest extends AbstractRpgServiceTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-ADMLRN400-01: 既存ページを更新する")
        void updatesExistingPage() {
            AdminLearn400Service.AdminLearnSession session =
                    adminLearn.start("LRN400STR");
            session.setInContent("Updated");

            adminLearn.save(session);

            assertThat(pages.findById(new Lrn400PageId("LRN400STR", 1))
                    .orElseThrow().getContent()).isEqualTo("Updated");
        }

        @Test
        @DisplayName("BR-ADMLRN400-02: F5で管理ページを進める")
        void movesForward() {
            AdminLearn400Service.AdminLearnSession session =
                    adminLearn.start("LRN400STR");

            adminLearn.pageFwd(session);

            assertThat(session.getCurPageNbr()).isEqualTo(2);
        }

        @Test
        @DisplayName("BR-ADMLRN400-03: F8で管理ページを戻す")
        void movesBack() {
            AdminLearn400Service.AdminLearnSession session =
                    adminLearn.start("LRN400STR");
            adminLearn.pageFwd(session);

            adminLearn.pageBack(session);

            assertThat(session.getCurPageNbr()).isEqualTo(1);
            assertThat(session.getNewOrUpd()).isZero();
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-ADMLRN400-04: ページ無し位置はNEWモードになる")
        void entersNewMode() {
            AdminLearn400Service.AdminLearnSession session =
                    adminLearn.start("LRN400STR");
            adminLearn.pageFwd(session);
            adminLearn.pageFwd(session);
            adminLearn.pageFwd(session);
            adminLearn.pageFwd(session);

            assertThat(session.getNewOrUpd()).isEqualTo(1);
            assertThat(session.isIn60()).isFalse();
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-ADMLRN400-05: NEWページを保存して作成する")
        void createsNewPage() {
            AdminLearn400Service.AdminLearnSession session =
                    adminLearn.start("LRN400STR");
            adminLearn.pageFwd(session);
            adminLearn.pageFwd(session);
            adminLearn.pageFwd(session);
            adminLearn.pageFwd(session);
            session.setInContent("New page");

            adminLearn.save(session);

            assertThat(pages.findById(new Lrn400PageId("LRN400STR", 5))
                    .orElseThrow().getContent()).isEqualTo("New page");
        }

        @Test
        @DisplayName("BR-ADMLRN400-06: 入力内容をDDS桁で切り捨てて保存する")
        void truncatesSavedFields() {
            AdminLearn400Service.AdminLearnSession session =
                    adminLearn.start("LRN400STR");
            session.setInContent("x".repeat(1600));
            session.setInExtra("1234567890");

            adminLearn.save(session);

            Lrn400Page saved = pages.findById(new Lrn400PageId("LRN400STR", 1))
                    .orElseThrow();
            assertThat(saved.getContent()).hasSize(1500);
            assertThat(saved.getExtra()).hasSize(9);
        }
    }
}
