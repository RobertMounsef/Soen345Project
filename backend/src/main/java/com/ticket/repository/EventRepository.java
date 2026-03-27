package com.ticket.repository;

import com.google.firebase.database.*;
import com.ticket.model.Event;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Repository
public class EventRepository {

    private final DatabaseReference eventsRef;

    public EventRepository(DatabaseReference rootRef) {
        this.eventsRef = rootRef.child("events");
    }

    public Event save(Event event) {
        DatabaseReference ref = (event.getEventId() == null)
                ? eventsRef.push()
                : eventsRef.child(event.getEventId());

        event.setEventId(ref.getKey());

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<DatabaseError> errorRef = new AtomicReference<>();

        ref.setValue(toMap(event), (error, ref1) -> {
            errorRef.set(error);
            latch.countDown();
        });

        await(latch);
        if (errorRef.get() != null) {
            throw new RuntimeException("Failed to save event: " + errorRef.get().getMessage());
        }
        return event;
    }

    public Optional<Event> findById(String id) {
        DataSnapshot snapshot = getSnapshot(eventsRef.child(id));
        return snapshot.exists() ? Optional.of(fromSnapshot(snapshot)) : Optional.empty();
    }

    public List<Event> findAll() {
        DataSnapshot snapshot = getSnapshot(eventsRef);
        List<Event> events = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            events.add(fromSnapshot(child));
        }
        return events;
    }

    public void deleteById(String id) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<DatabaseError> errorRef = new AtomicReference<>();

        eventsRef.child(id).removeValue((error, ref) -> {
            errorRef.set(error);
            latch.countDown();
        });

        await(latch);
        if (errorRef.get() != null) {
            throw new RuntimeException("Failed to delete event: " + errorRef.get().getMessage());
        }
    }

    public List<Event> findByCategoryIgnoreCase(String category) {
        return findAll().stream()
                .filter(e -> category.equalsIgnoreCase(e.getCategory()))
                .collect(Collectors.toList());
    }

    public List<Event> findByLocationContainingIgnoreCase(String location) {
        return findAll().stream()
                .filter(e -> e.getLocation() != null &&
                             e.getLocation().toLowerCase().contains(location.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Event> findByEventDateBetween(LocalDateTime start, LocalDateTime end) {
        return findAll().stream()
                .filter(e -> e.getEventDate() != null &&
                             !e.getEventDate().isBefore(start) &&
                             !e.getEventDate().isAfter(end))
                .collect(Collectors.toList());
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

    private Map<String, Object> toMap(Event e) {
        Map<String, Object> m = new HashMap<>();
        m.put("eventId", e.getEventId());
        m.put("organizerId", e.getOrganizerId());
        m.put("title", e.getTitle());
        m.put("category", e.getCategory());
        m.put("eventDate", e.getEventDate() != null ? e.getEventDate().toString() : null);
        m.put("location", e.getLocation());
        m.put("totalSpots", e.getTotalSpots());
        m.put("availableSpots", e.getAvailableSpots());
        m.put("status", e.getStatus() != null ? e.getStatus().name() : null);
        return m;
    }

    private Event fromSnapshot(DataSnapshot s) {
        Event e = new Event();
        e.setEventId(getString(s, "eventId"));
        e.setOrganizerId(getString(s, "organizerId"));
        e.setTitle(getString(s, "title"));
        e.setCategory(getString(s, "category"));
        String eventDate = getString(s, "eventDate");
        if (eventDate != null) e.setEventDate(LocalDateTime.parse(eventDate));
        e.setLocation(getString(s, "location"));

        Object totalSpots = s.child("totalSpots").getValue();
        if (totalSpots instanceof Number) e.setTotalSpots(((Number) totalSpots).intValue());

        Object availableSpots = s.child("availableSpots").getValue();
        if (availableSpots instanceof Number) e.setAvailableSpots(((Number) availableSpots).intValue());

        String status = getString(s, "status");
        if (status != null) e.setStatus(Event.Status.valueOf(status));
        return e;
    }

    private String getString(DataSnapshot s, String key) {
        Object val = s.child(key).getValue();
        return val != null ? val.toString() : null;
    }
}