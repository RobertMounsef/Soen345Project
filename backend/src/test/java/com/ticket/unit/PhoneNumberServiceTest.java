package com.ticket.unit;

import com.ticket.service.PhoneNumberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PhoneNumberService")
class PhoneNumberServiceTest {

    private final PhoneNumberService service = new PhoneNumberService("CA");

    @Nested
    @DisplayName("normalizeToE164")
    class Normalize {

        @Test
        @DisplayName("formats 10-digit Canadian numbers to E.164")
        void canadianTenDigit() {
            assertThat(service.normalizeToE164("5145550199")).isEqualTo("+15145550199");
        }

        @Test
        @DisplayName("accepts already-normalized E.164")
        void alreadyE164() {
            assertThat(service.normalizeToE164("+15145550199")).isEqualTo("+15145550199");
        }

        @Test
        @DisplayName("rejects invalid numbers")
        void invalid() {
            assertThatThrownBy(() -> service.normalizeToE164("123"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid phone");
        }
    }

    @Nested
    @DisplayName("tryNormalizeToE164")
    class TryNormalize {

        @Test
        @DisplayName("returns empty for blank input")
        void blank() {
            assertThat(service.tryNormalizeToE164("  ")).isEmpty();
        }

        @Test
        @DisplayName("normalizes valid input")
        void ok() {
            assertThat(service.tryNormalizeToE164("5145550199")).contains("+15145550199");
        }
    }
}
