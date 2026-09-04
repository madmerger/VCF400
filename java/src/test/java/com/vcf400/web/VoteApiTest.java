package com.vcf400.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class VoteApiTest extends AbstractWebIntegrationTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-ADDVOTE-05/API: 有効な投票はHTTP200で成功する")
        void acceptsVote() {
            ResponseEntity<Map> response = rest.postForEntity(
                    "/api/vote",
                    Map.of(
                            "badge", 1001,
                            "exhibitId", "ASHIBATA",
                            "award", 1,
                            "profile", "ASHIBATA"),
                    Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).containsEntry("success", true);
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-ADDVOTE-05/API: 同じbadgeの再投票を拒否する")
        void rejectsDuplicateVote() {
            Map<String, Object> body = Map.of(
                    "badge", 1002,
                    "exhibitId", "ASHIBATA",
                    "award", 1,
                    "profile", "ASHIBATA");
            rest.postForEntity("/api/vote", body, Map.class);

            ResponseEntity<Map> response =
                    rest.postForEntity("/api/vote", body, Map.class);

            assertThat(response.getBody())
                    .containsEntry("errLine", "You have already voted.");
        }

        @Test
        @DisplayName("BR-ADDVOTE-02/API: 必須エラーをJSONで返す")
        void returnsVoteValidationError() {
            ResponseEntity<Map> response = rest.postForEntity(
                    "/api/vote",
                    Map.of("badge", 0, "exhibitId", "", "award", 0, "profile", "MM2024"),
                    Map.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody())
                    .containsEntry("success", false)
                    .containsEntry("errLine", "Must enter badge number");
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-ADDVOTE-07/API: eligible=0の投票を拒否する")
        void rejectsIneligibleVote() {
            ResponseEntity<Map> response = rest.postForEntity(
                    "/api/vote",
                    Map.of(
                            "badge", 1003,
                            "exhibitId", "NOVOTE",
                            "award", 1,
                            "profile", "MM2024"),
                    Map.class);

            assertThat(response.getBody())
                    .containsEntry("errLine", "Exhibit ineligible for award");
        }
    }
}
