package com.ticket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring context integration test.
 * Requires a valid firebase-service-account.json in src/main/resources to run.
 * Disabled in CI/unit test runs — enable manually when the service account key is present.
 */
@SpringBootTest
class BackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
