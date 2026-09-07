package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.vcf400.domain.GuestbookComment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AddGuestbookCommentServiceTest extends AbstractRpgServiceTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-ADDGBCMT-01/04: 空DBではコメントID=1で登録する")
        void assignsFirstCommentId() {
            AddGuestbookCommentService.CommentResult result =
                    addComment.submit("GERTIE", "NOVOTE", "Name", "Text");

            assertThat(result.success()).isTrue();
            assertThat(result.commentId()).isEqualTo(1);
        }

        @Test
        @DisplayName("BR-ADDGBCMT-04: コメントはVISIBLE=Yで登録する")
        void savesVisibleComment() {
            addComment.submit("GERTIE", "GERTIE", "Name", "Text");

            assertThat(comments.findById(1).orElseThrow().getVisible())
                    .isEqualTo("Y");
        }

        @Test
        @DisplayName("BR-ADDGBCMT-05: コメント登録時にPRTLSTCMTスプールへ出力する")
        void printsCommentTicket() {
            addComment.submit("GERTIE", "GERTIE", "Name", "Text");

            assertThat(spool.spool())
                    .anyMatch(value -> value.contains("CMTID=1"));
        }

        @Test
        @DisplayName("BR-ADDGBCMT-03: MM2024は指定した出展IDへ投稿する")
        void mm2024AllowsFreeExhibitInput() {
            addComment.submit("MM2024", "GERTIE", "Name", "Text");

            assertThat(comments.findById(1).orElseThrow().getExhbId())
                    .isEqualTo("GERTIE");
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-ADDGBCMT-01: exhibit空欄は必須エラー")
        void rejectsMissingExhibit() {
            assertThat(addComment.submit("MM2024", "", "Name", "Text").errLine())
                    .isEqualTo(RpgMessages.COMMENT_EXHIBIT);
        }

        @Test
        @DisplayName("BR-ADDGBCMT-02: name空欄は必須エラー")
        void rejectsMissingName() {
            assertThat(addComment.submit("GERTIE", "GERTIE", "", "Text").errLine())
                    .isEqualTo(RpgMessages.NO_NAME);
        }

        @Test
        @DisplayName("BR-ADDGBCMT-03: comment空欄は必須エラー")
        void rejectsMissingComment() {
            assertThat(addComment.submit("GERTIE", "GERTIE", "Name", "").errLine())
                    .isEqualTo(RpgMessages.NO_COMMENT);
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-ADDGBCMT-04: コメントIDは最大値+1を採番する")
        void assignsMaximumIdPlusOne() {
            comments.save(new GuestbookComment(7, "Y", "GERTIE", "Name", "Text"));

            assertThat(addComment.submit("GERTIE", "GERTIE", "Name", "Text").commentId())
                    .isEqualTo(8);
        }

        @Test
        @DisplayName("BR-ADDGBCMT-05: nameはDDS桁16で切り捨てる")
        void truncatesName() {
            addComment.submit(
                    "GERTIE",
                    "GERTIE",
                    "12345678901234567890",
                    "Text");

            assertThat(comments.findById(1).orElseThrow().getGuestName())
                    .hasSize(16);
        }
    }
}
