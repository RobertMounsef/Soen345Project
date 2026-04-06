package com.ticket.integration.firebase;

import com.google.firebase.database.DatabaseReference;
import com.ticket.BackendApplication;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

/**
 * Boots the Spring context with a dedicated RTDB subtree so repository tests do not clash with
 * production data. Requires {@code firebase-service-account.json} on the classpath (e.g.
 * {@code src/main/resources}).
 *
 * <p>Tagged {@code firebase-integration} — excluded from default {@code mvn test}; run with
 * {@code mvn verify -Pfirebase-it}.
 */
@SpringBootTest(classes = BackendApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("integration-firebase")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("firebase-integration")
public abstract class AbstractFirebaseRepositoryIT {

    @Autowired
    protected DatabaseReference databaseRoot;

    @DynamicPropertySource
    static void isolateFirebaseTree(DynamicPropertyRegistry registry) {
        registry.add(
                "firebase.database.path-prefix",
                () -> "integration_test_" + UUID.randomUUID().toString().replace("-", ""));
    }

    @AfterAll
    void deleteIsolationBranch() {
        if (databaseRoot != null) {
            FirebaseTestSupport.removeValue(databaseRoot);
        }
    }
}
