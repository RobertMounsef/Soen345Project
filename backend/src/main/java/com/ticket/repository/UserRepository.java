package com.ticket.repository;

import com.google.firebase.database.*;
import com.ticket.model.User;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Repository
public class UserRepository {

    private final DatabaseReference usersRef;

    public UserRepository(DatabaseReference rootRef) {
        this.usersRef = rootRef.child("users");
    }

    /** Persist a user. If userId is null a new push key is generated. */
    public User save(User user) {
        DatabaseReference ref = (user.getUserId() == null)
                ? usersRef.push()
                : usersRef.child(user.getUserId());

        user.setUserId(ref.getKey());
        user.setLastUpdate(LocalDateTime.now());

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<DatabaseError> errorRef = new AtomicReference<>();

        ref.setValue(toMap(user), (error, ref1) -> {
            errorRef.set(error);
            latch.countDown();
        });

        await(latch);
        if (errorRef.get() != null) {
            throw new RuntimeException("Failed to save user: " + errorRef.get().getMessage());
        }
        return user;
    }

    public Optional<User> findById(String id) {
        DataSnapshot snapshot = getSnapshot(usersRef.child(id));
        return snapshot.exists() ? Optional.of(fromSnapshot(snapshot)) : Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        String needle = email.trim();
        return findAll().stream()
                .filter(u -> u.getEmail() != null
                        && needle.equalsIgnoreCase(u.getEmail().trim()))
                .findFirst();
    }

    public Optional<User> findByPhone(String phone) {
        return findAll().stream()
                .filter(u -> phone.equals(u.getPhone()))
                .findFirst();
    }

    public Optional<User> findByName(String name) {
        return findAll().stream()
                .filter(u -> name.equals(u.getName()))
                .findFirst();
    }

    public List<User> findAll() {
        DataSnapshot snapshot = getSnapshot(usersRef);
        List<User> users = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            users.add(fromSnapshot(child));
        }
        return users;
    }

    public void deleteById(String id) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<DatabaseError> errorRef = new AtomicReference<>();

        usersRef.child(id).removeValue((error, ref) -> {
            errorRef.set(error);
            latch.countDown();
        });

        await(latch);
        if (errorRef.get() != null) {
            throw new RuntimeException("Failed to delete user: " + errorRef.get().getMessage());
        }
    }

    // ---- Helpers ----

    private DataSnapshot getSnapshot(DatabaseReference ref) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<DataSnapshot> snapshotRef = new AtomicReference<>();
        AtomicReference<DatabaseError> errorRef = new AtomicReference<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot snapshot) {
                snapshotRef.set(snapshot);
                latch.countDown();
            }
            @Override public void onCancelled(DatabaseError error) {
                errorRef.set(error);
                latch.countDown();
            }
        });

        await(latch);
        if (errorRef.get() != null) {
            throw new RuntimeException("Firebase read error: " + errorRef.get().getMessage());
        }
        return snapshotRef.get();
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Firebase operation interrupted", e);
        }
    }

    private Map<String, Object> toMap(User u) {
        Map<String, Object> m = new HashMap<>();
        m.put("userId", u.getUserId());
        m.put("name", u.getName());
        m.put("email", u.getEmail());
        m.put("phone", u.getPhone());
        m.put("password", u.getPassword());
        m.put("role", u.getRole() != null ? u.getRole().name() : null);
        m.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
        m.put("lastUpdate", u.getLastUpdate() != null ? u.getLastUpdate().toString() : null);
        return m;
    }

    private User fromSnapshot(DataSnapshot s) {
        User u = new User();
        u.setUserId(getString(s, "userId"));
        u.setName(getString(s, "name"));
        u.setEmail(getString(s, "email"));
        u.setPhone(getString(s, "phone"));
        u.setPassword(getString(s, "password"));
        String role = getString(s, "role");
        if (role != null) u.setRole(User.Role.valueOf(role));
        String createdAt = getString(s, "createdAt");
        if (createdAt != null) u.setCreatedAt(LocalDateTime.parse(createdAt));
        String lastUpdate = getString(s, "lastUpdate");
        if (lastUpdate != null) u.setLastUpdate(LocalDateTime.parse(lastUpdate));
        return u;
    }

    private String getString(DataSnapshot s, String key) {
        Object val = s.child(key).getValue();
        return val != null ? val.toString() : null;
    }
}