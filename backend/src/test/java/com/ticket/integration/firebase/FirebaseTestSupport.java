package com.ticket.integration.firebase;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

final class FirebaseTestSupport {

    private FirebaseTestSupport() {
    }

    static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted waiting on Firebase", e);
        }
    }

    /** Deletes the given node and all children (async removeValue on that ref). */
    static void removeValue(DatabaseReference ref) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<DatabaseError> errorRef = new AtomicReference<>();
        ref.removeValue((error, r) -> {
            errorRef.set(error);
            latch.countDown();
        });
        await(latch);
        DatabaseError err = errorRef.get();
        if (err != null) {
            throw new IllegalStateException("Firebase delete failed: " + err.getMessage());
        }
    }

}
