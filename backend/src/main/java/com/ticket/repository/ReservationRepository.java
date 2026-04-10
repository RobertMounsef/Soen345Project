package com.ticket.repository;

import com.google.firebase.database.*;
import com.ticket.model.Reservation;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Repository
public class ReservationRepository {

    private final DatabaseReference reservationsRef;

    public ReservationRepository(DatabaseReference rootRef) {
        this.reservationsRef = rootRef.child("reservations");
    }

    public Reservation save(Reservation reservation) {
        DatabaseReference ref = (reservation.getReservationId() == null)
                ? reservationsRef.push()
                : reservationsRef.child(reservation.getReservationId());

        reservation.setReservationId(ref.getKey());

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<DatabaseError> errorRef = new AtomicReference<>();

        ref.setValue(toMap(reservation), (error, ref1) -> {
            errorRef.set(error);
            latch.countDown();
        });

        await(latch);
        if (errorRef.get() != null) {
            throw new RuntimeException("Failed to save reservation: " + errorRef.get().getMessage());
        }
        return reservation;
    }

    public Optional<Reservation> findById(String id) {
        DataSnapshot snapshot = getSnapshot(reservationsRef.child(id));
        return snapshot.exists() ? Optional.of(fromSnapshot(snapshot)) : Optional.empty();
    }

    public List<Reservation> findAll() {
        DataSnapshot snapshot = getSnapshot(reservationsRef);
        List<Reservation> list = new ArrayList<>();
        for (DataSnapshot child : snapshot.getChildren()) {
            list.add(fromSnapshot(child));
        }
        return list;
    }

    public void deleteById(String id) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<DatabaseError> errorRef = new AtomicReference<>();

        reservationsRef.child(id).removeValue((error, ref) -> {
            errorRef.set(error);
            latch.countDown();
        });

        await(latch);
        if (errorRef.get() != null) {
            throw new RuntimeException("Failed to delete reservation: " + errorRef.get().getMessage());
        }
    }

    public List<Reservation> findByUserId(String userId) {
        return findAll().stream()
                .filter(r -> userId.equals(r.getUserId()))
                .collect(Collectors.toList());
    }

    public List<Reservation> findByEventId(String eventId) {
        return findAll().stream()
                .filter(r -> eventId.equals(r.getEventId()))
                .collect(Collectors.toList());
    }

    public Optional<Reservation> findByEventIdAndUserId(String eventId, String userId) {
        return findAll().stream()
                .filter(r -> eventId.equals(r.getEventId()) && userId.equals(r.getUserId()))
                .findFirst();
    }

    public boolean existsByUserIdAndEventIdAndStatus(
            String userId, String eventId, Reservation.Status status) {
        return findAll().stream()
                .anyMatch(r -> userId.equals(r.getUserId())
                            && eventId.equals(r.getEventId())
                            && status.equals(r.getStatus()));
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

    private Map<String, Object> toMap(Reservation r) {
        Map<String, Object> m = new HashMap<>();
        m.put("reservationId", r.getReservationId());
        m.put("userId", r.getUserId());
        m.put("userName", r.getUserName());
        m.put("eventId", r.getEventId());
        m.put("eventTitle", r.getEventTitle());
        m.put("eventDate", r.getEventDate());
        m.put("eventLocation", r.getEventLocation());
        m.put("reservationDate", r.getReservationDate() != null ? r.getReservationDate().toString() : null);
        m.put("status", r.getStatus() != null ? r.getStatus().name() : null);
        return m;
    }

    private Reservation fromSnapshot(DataSnapshot s) {
        Reservation r = new Reservation();
        r.setReservationId(getString(s, "reservationId"));
        r.setUserId(getString(s, "userId"));
        r.setUserName(getString(s, "userName"));
        r.setEventId(getString(s, "eventId"));
        r.setEventTitle(getString(s, "eventTitle"));
        r.setEventDate(getString(s, "eventDate"));
        r.setEventLocation(getString(s, "eventLocation"));
        String date = getString(s, "reservationDate");
        if (date != null) r.setReservationDate(LocalDateTime.parse(date));
        String status = getString(s, "status");
        if (status != null) r.setStatus(Reservation.Status.valueOf(status));
        return r;
    }

    private String getString(DataSnapshot s, String key) {
        Object val = s.child(key).getValue();
        return val != null ? val.toString() : null;
    }
}