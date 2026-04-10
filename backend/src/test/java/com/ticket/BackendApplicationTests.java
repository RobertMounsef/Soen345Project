package com.ticket;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring context integration test.
 * Requires a valid firebase-service-account.json in src/main/resources to run.
 * Disabled in CI/unit test runs — enable manually when the service account key is present.
 */
@SpringBootTest
@Disabled("Requires firebase-service-account.json — run manually after placing the key in src/main/resources/")
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
