package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.vcf400.domain.GuestbookComment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AdminHideCommentServiceTest extends AbstractRpgServiceTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-ADMHIDECMT-01: コメントを読み込む")
        void readsComment() {
            comments.save(new GuestbookComment(1, "Y", "GERTIE", "Name", "Text"));

            assertThat(hideComment.read(1).getGuestCmt()).isEqualTo("Text");
        }

        @Test
        @DisplayName("BR-ADMHIDECMT-02: Yでコメントを表示状態にする")
        void setsVisible() {
            comments.save(new GuestbookComment(1, "N", "GERTIE", "Name", "Text"));

            assertThat(hideComment.update(1, "Y").comment().getVisible())
                    .isEqualTo("Y");
        }

        @Test
        @DisplayName("BR-ADMHIDECMT-03: Nでコメントを非表示にする")
        void setsHidden() {
            comments.save(new GuestbookComment(1, "Y", "GERTIE", "Name", "Text"));

            assertThat(hideComment.update(1, "N").comment().getVisible())
                    .isEqualTo("N");
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-ADMHIDECMT-04: Y/N以外は不正値エラー")
        void rejectsInvalidVisibility() {
            comments.save(new GuestbookComment(1, "Y", "GERTIE", "Name", "Text"));

            assertThat(hideComment.update(1, "X").message())
                    .isEqualTo(RpgMessages.INVALID_YN);
        }

        @Test
        @DisplayName("BR-ADMHIDECMT-01: 不在IDはコメントIDエラー")
        void rejectsMissingComment() {
            assertThat(hideComment.update(9, "Y").message())
                    .isEqualTo(RpgMessages.COMMENT_ID);
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-ADMHIDECMT-02: 更新後の状態をDBへ保存する")
        void persistsVisibility() {
            comments.save(new GuestbookComment(1, "Y", "GERTIE", "Name", "Text"));

            hideComment.update(1, "N");

            assertThat(comments.findById(1).orElseThrow().getVisible()).isEqualTo("N");
        }
    }
}
