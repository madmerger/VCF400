package com.vcf400.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DdsFieldTest {

    @Nested
    class Normal {

        @Test
        @DisplayName("DDS: 桁以内の値は変更しない")
        void preservesValueWithinLength() {
            assertThat(DdsField.truncate("abc", 3)).isEqualTo("abc");
        }
    }

    @Nested
    class Error {

        @Test
        @DisplayName("DDS: nullはnullのまま返す")
        void preservesNull() {
            assertThat(DdsField.truncate(null, 3)).isNull();
        }
    }

    @Nested
    class Boundary {

        @Test
        @DisplayName("DDS: 超過分を切り捨てる")
        void truncatesExcess() {
            assertThat(DdsField.truncate("abcd", 3)).isEqualTo("abc");
        }

        @Test
        @DisplayName("DDS: 長さ0では空文字にする")
        void truncatesToZero() {
            assertThat(DdsField.truncate("abc", 0)).isEmpty();
        }
    }
}
