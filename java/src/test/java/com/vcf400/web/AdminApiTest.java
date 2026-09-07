package com.vcf400.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class AdminApiTest extends AbstractWebIntegrationTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-ADMCRTEXHB-03/API: 新規出展を登録できる")
        void createsExhibit() {
            ResponseEntity<Map> response = rest.postForEntity(
                    "/api/admin/exhibits",
                    Map.of(
                            "profile", "NEWONE",
                            "owner", "Owner",
                            "city", "Boston",
                            "state", "MA",
                            "title", "Title",
                            "description", "Description",
                            "eligible", 1,
                            "learn400", 1),
                    Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("success", true);
        }

        @Test
        @DisplayName("BR-ADMHIDECMT-02/API: コメント表示状態を更新できる")
        void updatesCommentVisibility() {
            ResponseEntity<Map> response = rest.postForEntity(
                    "/api/admin/comments/1/visibility?value=N",
                    Map.of(),
                    Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-ADMCRTEXHB-04/API: ASHIBATAの新規登録は重複エラー")
        void rejectsDuplicateExhibit() {
            ResponseEntity<Map> response = rest.postForEntity(
                    "/api/admin/exhibits",
                    Map.of(
                            "profile", "ASHIBATA",
                            "owner", "Duplicate",
                            "city", "Boston",
                            "state", "MA",
                            "title", "Duplicate",
                            "description", "Duplicate",
                            "eligible", 1,
                            "learn400", 1),
                    Map.class);

            assertThat(response.getBody())
                    .containsEntry("message", "Error - USRPRF exists");
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-ADMVOTERPT-01/API: 投票集計APIを取得できる")
        void readsVoteReport() {
            ResponseEntity<Map> response =
                    rest.getForEntity("/api/admin/votes/ASHIBATA", Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsKey("totalVotes");
        }
    }
}
