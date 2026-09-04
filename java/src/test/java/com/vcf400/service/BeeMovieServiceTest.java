package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.vcf400.domain.BeeMovieLine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BeeMovieServiceTest extends AbstractRpgServiceTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("BR-BEEMOVIE-01: BEEMOVIEをLINE順に全件返す")
        void returnsAllLinesInOrder() {
            beeMovieLines.save(new BeeMovieLine("Speaker 2", "02"));
            beeMovieLines.save(new BeeMovieLine("Speaker 1", "01"));

            assertThat(beeMovie.all())
                    .extracting(BeeMovieLine::getLine)
                    .containsExactly("01", "02");
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("BR-BEEMOVIE-02: 空DBでは空一覧を返す")
        void returnsEmptyList() {
            assertThat(beeMovie.all()).isEmpty();
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("BR-BEEMOVIE-01: 同一取得結果を保持する")
        void returnsPersistedSpeaker() {
            beeMovieLines.save(new BeeMovieLine("Speaker", "01"));

            assertThat(beeMovie.all().get(0).getSpeaker()).isEqualTo("Speaker");
        }
    }
}
