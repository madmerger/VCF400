package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.vcf400.domain.GuestbookComment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReadGuestbookCommentServiceTest extends AbstractRpgServiceTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-READGBCMT-03: 公開コメントを同じ出展者が閲覧できる")
        void readsOwnVisibleComment() {
            comments.save(new GuestbookComment(4, "Y", "GERTIE", "Alice", "Hello"));

            ReadGuestbookCommentService.CommentView result =
                    readComment.read("GERTIE", 4);

            assertThat(result.outName()).isEqualTo("Alice");
            assertThat(result.outCmt()).isEqualTo("Hello");
            assertThat(result.outTitle()).isEqualTo("Gertie title");
        }

        @Test
        @DisplayName("BR-READGBCMT-04: MM2024は他出展者の公開コメントも閲覧できる")
        void mm2024ReadsAnyVisibleComment() {
            comments.save(new GuestbookComment(4, "Y", "GERTIE", "Alice", "Hello"));

            assertThat(readComment.read("MM2024", 4).outName())
                    .isEqualTo("Alice");
        }

        @Test
        @DisplayName("BR-READGBCMT-02: 非表示コメントはName Hiddenを表示する")
        void readsHiddenName() {
            comments.save(new GuestbookComment(9, "N", "GERTIE", "Bob", "Hidden"));

            assertThat(readComment.read("GERTIE", 9).outName())
                    .isEqualTo(RpgMessages.HIDDEN_NAME);
        }

        @Test
        @DisplayName("BR-READGBCMT-02: 非表示コメントは非表示メッセージを表示する")
        void readsHiddenComment() {
            comments.save(new GuestbookComment(9, "N", "GERTIE", "Bob", "Hidden"));

            assertThat(readComment.read("GERTIE", 9).outCmt())
                    .isEqualTo(RpgMessages.HIDDEN_COMMENT);
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-READGBCMT-01: CommentID=0は必須エラー")
        void rejectsZeroCommentId() {
            assertThat(readComment.read("GERTIE", 0).errLine())
                    .isEqualTo(RpgMessages.COMMENT_ID);
        }

        @Test
        @DisplayName("BR-READGBCMT-03: 他出展者はERRPRIVメッセージを表示する")
        void rejectsOtherExhibit() {
            comments.save(new GuestbookComment(4, "Y", "GERTIE", "Alice", "Hello"));

            assertThat(readComment.read("BEEBOX", 4).outCmt())
                    .isEqualTo(RpgMessages.PRIVATE_COMMENT);
        }

        @Test
        @DisplayName("BR-READGBCMT-04: 存在しないIDはERRPRIVメッセージを表示する")
        void rejectsMissingComment() {
            assertThat(readComment.read("GERTIE", 99).outCmt())
                    .isEqualTo(RpgMessages.PRIVATE_COMMENT);
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-READGBCMT-05: 総コメント数は最大CMTIDを返す")
        void reportsMaximumCommentId() {
            comments.save(new GuestbookComment(4, "Y", "GERTIE", "Alice", "Hello"));
            comments.save(new GuestbookComment(9, "N", "GERTIE", "Bob", "Hidden"));

            assertThat(readComment.read("GERTIE", 4).totalComments())
                    .isEqualTo(9);
        }

        @Test
        @DisplayName("BR-READGBCMT-01: profileはDDS桁20で切り捨てる")
        void truncatesProfile() {
            comments.save(new GuestbookComment(4, "Y", "GERTIE", "Alice", "Hello"));

            assertThat(readComment.read("GERTIEXXXXXXXXXXXXXXXXX", 4).errLine())
                    .hasSize(20);
        }
    }
}
