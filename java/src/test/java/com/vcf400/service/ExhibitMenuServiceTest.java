package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.vcf400.domain.Exhibit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ExhibitMenuServiceTest extends AbstractRpgServiceTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-EXHBMENU-01: 出展情報を表示する")
        void loadsExhibit() {
            ExhibitMenuService.MenuView view = menu.load("GERTIE");

            assertThat(view.title()).isEqualTo("Gertie title");
            assertThat(view.name()).isEqualTo("Gertie");
            assertThat(view.city()).isEqualTo("Boston");
        }

        @Test
        @DisplayName("BR-EXHBMENU-02: eligible=1では投票を表示する")
        void showsVoteForEligibleExhibit() {
            assertThat(menu.load("GERTIE").showVote()).isTrue();
        }

        @Test
        @DisplayName("BR-EXHBMENU-03: ENLRN400=1ではLEARN/400を表示する")
        void showsLearnForLearnEnabledExhibit() {
            assertThat(menu.load("GERTIE").showLrn400()).isTrue();
        }

        @Test
        @DisplayName("BR-EXHBMENU-04: 選択1はVOTESTUBへ遷移する")
        void selectsVote() {
            assertThat(menu.select("GERTIE", 1)).isEqualTo("VOTESTUB");
        }

        @Test
        @DisplayName("BR-EXHBMENU-05: 選択2はLRN400STUBへ遷移する")
        void selectsLearn() {
            assertThat(menu.select("GERTIE", 2)).isEqualTo("LRN400STUB");
        }

        @Test
        @DisplayName("BR-EXHBMENU-06: 選択3はADDGBSTUBへ遷移する")
        void selectsAddGuestbook() {
            assertThat(menu.select("GERTIE", 3)).isEqualTo("ADDGBSTUB");
        }

        @Test
        @DisplayName("BR-EXHBMENU-07: 選択4はREADGBSTUBへ遷移する")
        void selectsReadGuestbook() {
            assertThat(menu.select("GERTIE", 4)).isEqualTo("READGBSTUB");
        }

        @Test
        @DisplayName("BR-EXHBMENU-08: 選択7はADMKIOSKへ遷移する")
        void selectsKioskExit() {
            assertThat(menu.select("GERTIE", 7)).isEqualTo("ADMKIOSK");
        }

        @Test
        @DisplayName("BR-EXHBMENU-09: 正しいキオスクパスワードを受け入れる")
        void acceptsKioskPassword() {
            assertThat(menu.exitKiosk("VCF400")).isTrue();
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-EXHBMENU-10: eligible=0では投票を表示しない")
        void hidesVoteForIneligibleExhibit() {
            assertThat(menu.load("NOVOTE").showVote()).isFalse();
        }

        @Test
        @DisplayName("BR-EXHBMENU-11: 不在profileは投票とLEARN/400を表示しない")
        void handlesMissingProfile() {
            ExhibitMenuService.MenuView view = menu.load("MISSING");

            assertThat(view.showVote()).isFalse();
            assertThat(view.showLrn400()).isFalse();
        }

        @Test
        @DisplayName("BR-EXHBMENU-12: ALWVOTE=Nでは投票画面へ遷移しない")
        void blocksVoteWhenDisabled() {
            settings.findById("ALWVOTE").orElseThrow().setValue("N");

            assertThat(menu.select("GERTIE", 1)).isEqualTo("MENU");
        }

        @Test
        @DisplayName("BR-EXHBMENU-13: 間違ったキオスクパスワードを拒否する")
        void rejectsKioskPassword() {
            assertThat(menu.exitKiosk("wrong")).isFalse();
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-EXHBMENU-14: profileはDDS桁9で切り捨てる")
        void truncatesProfile() {
            exhibits.save(new Exhibit(
                    3,
                    "GERTIEXXX",
                    "Owner",
                    "City",
                    "MA",
                    "Truncated",
                    "Description",
                    1,
                    1));

            assertThat(menu.load("GERTIEXXXX").title()).isEqualTo("Truncated");
        }
    }
}
