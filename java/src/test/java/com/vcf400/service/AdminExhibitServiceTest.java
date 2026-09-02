package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.vcf400.domain.Exhibit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AdminExhibitServiceTest extends AbstractRpgServiceTest {

    private AdminExhibitService.ExhibitEditorSession newSession(String profile) {
        AdminExhibitService.ExhibitEditorSession session =
                exhibitAdmin.loadSession("NONE");
        session.setProfile(profile);
        session.setOwner("Owner");
        session.setCity("City");
        session.setState("MA");
        session.setTitle("Title");
        session.setDescription("Description");
        session.setEligible(1);
        session.setLearn400(1);
        return session;
    }

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-ADMCRTEXHB-01: NONEは新規モードでロードする")
        void loadsNone() {
            assertThat(exhibitAdmin.loadSession("NONE").getNewOrEdt()).isZero();
        }

        @Test
        @DisplayName("BR-ADMCRTEXHB-02: 既存出展を編集モードでロードする")
        void loadsExisting() {
            assertThat(exhibitAdmin.loadSession("GERTIE").getNewOrEdt()).isEqualTo(1);
        }

        @Test
        @DisplayName("BR-ADMCRTEXHB-03: 既存出展を更新する")
        void updatesExisting() {
            AdminExhibitService.ExhibitEditorSession session =
                    exhibitAdmin.loadSession("GERTIE");
            session.setTitle("Updated title");

            assertThat(exhibitAdmin.writeExhb(session).success()).isTrue();
            assertThat(exhibits.findById("GERTIE").orElseThrow().getExhBtitle())
                    .isEqualTo("Updated title");
        }

        @Test
        @DisplayName("BR-ADMCRTEXHB-04: 新規出展を作成する")
        void createsNew() {
            AdminExhibitService.ExhibitResult result =
                    exhibitAdmin.writeExhb(newSession("NEWONE"));

            assertThat(result.success()).isTrue();
            assertThat(exhibits.findById("NEWONE")).isPresent();
        }

        @Test
        @DisplayName("BR-ADMCRTEXHB-05: 出展一覧を返す")
        void listsExhibits() {
            assertThat(exhibitAdmin.list()).hasSize(2);
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-ADMCRTEXHB-02: 不在出展はERRSTS=2になる")
        void missingExhibitSetsStatusTwo() {
            AdminExhibitService.ExhibitEditorSession session =
                    exhibitAdmin.loadSession("MISSING");

            assertThat(session.getErrSts()).isEqualTo(2);
        }

        @Test
        @DisplayName("BR-ADMCRTEXHB-03: 既存出展への新規登録はERRSTS=1")
        void duplicateNewExhibitSetsStatusOne() {
            AdminExhibitService.ExhibitEditorSession session = newSession("GERTIE");

            assertThat(exhibitAdmin.writeExhb(session).status()).isEqualTo(1);
        }

        @Test
        @DisplayName("BR-ADMCRTEXHB-06: 不在出展の削除はERRSTS=3")
        void missingDeleteSetsStatusThree() {
            assertThat(exhibitAdmin.delRcd("MISSING", false).status()).isEqualTo(3);
        }

        @Test
        @DisplayName("BR-ADMCRTEXHB-07: 確認なしの削除は削除せず状態0")
        void unconfirmedDeleteKeepsRecord() {
            assertThat(exhibitAdmin.delRcd("GERTIE", false).status()).isZero();
            assertThat(exhibits.findById("GERTIE")).isPresent();
        }

        @Test
        @DisplayName("BR-ADMCRTEXHB-08: 空profileの登録はERRSTS=5")
        void blankProfileSetsStatusFive() {
            assertThat(exhibitAdmin.writeExhb(newSession("")).status()).isEqualTo(5);
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-ADMCRTEXHB-09: 確認済み削除はERRSTS=4で削除する")
        void confirmedDeleteSetsStatusFour() {
            assertThat(exhibitAdmin.delRcd("GERTIE", true).status()).isEqualTo(4);
            assertThat(exhibits.findById("GERTIE")).isEmpty();
        }

        @Test
        @DisplayName("BR-ADMCRTEXHB-10: 編集項目をDDS桁で切り捨てる")
        void truncatesEditorFields() {
            AdminExhibitService.ExhibitResult result =
                    exhibitAdmin.writeExhb(new AdminExhibitService.ExhibitForm(
                            "NEWONE",
                            "x".repeat(30),
                            "x".repeat(20),
                            "LONG",
                            "x".repeat(60),
                            "x".repeat(1100),
                            1,
                            1));

            Exhibit saved = result.exhibit();
            assertThat(saved.getExhBitor()).hasSize(20);
            assertThat(saved.getExhBcity()).hasSize(15);
            assertThat(saved.getExhBstate()).hasSize(2);
            assertThat(saved.getExhBtitle()).hasSize(50);
            assertThat(saved.getExhBdesc()).hasSize(1000);
        }
    }
}
