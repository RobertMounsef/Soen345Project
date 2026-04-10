package com.ticket.integration.firebase;

import com.ticket.model.Event;
import com.ticket.model.Reservation;
import com.ticket.model.User;
import com.ticket.repository.EventRepository;
import com.ticket.repository.ReservationRepository;
import com.ticket.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReservationRepository (Firebase integration)")
class ReservationRepositoryFirebaseIT extends AbstractFirebaseRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private User savedCustomer() {
        User u = new User();
        u.setName("IT Customer");
        u.setEmail("it-res-" + System.nanoTime() + "@example.com");
        u.setPassword("p");
        u.setRole(User.Role.CUSTOMER);
        return userRepository.save(u);
    }

    private Event savedEvent() {
        Event e = new Event();
        e.setTitle("IT Event " + System.nanoTime());
        e.setCategory("music");
        e.setLocation("Montreal");
        e.setEventDate(LocalDateTime.of(2026, 8, 1, 18, 0));
        e.setTotalSpots(10);
        e.setAvailableSpots(10);
        e.setStatus(Event.Status.ACTIVE);
        e.setOrganizerId("it-org");
        return eventRepository.save(e);
    }

    @Test
    @DisplayName("findByUserId returns reservations for that user")
    void findByUserId() {
        User u = savedCustomer();
        Event ev = savedEvent();

        Reservation r = new Reservation();
        r.setUserId(u.getUserId());
        r.setEventId(ev.getEventId());
        r.setReservationDate(LocalDateTime.now());
        r.setStatus(Reservation.Status.CONFIRMED);
        Reservation saved = reservationRepository.save(r);

        List<Reservation> list = reservationRepository.findByUserId(u.getUserId());
        assertThat(list).extracting(Reservation::getReservationId).contains(saved.getReservationId());
    }

    @Test
    @DisplayName("findByEventId returns reservations for that event")
    void findByEventId() {
        User u = savedCustomer();
        Event ev = savedEvent();

        Reservation r = new Reservation();
        r.setUserId(u.getUserId());
        r.setEventId(ev.getEventId());
        r.setStatus(Reservation.Status.CONFIRMED);
        Reservation saved = reservationRepository.save(r);

        List<Reservation> list = reservationRepository.findByEventId(ev.getEventId());
        assertThat(list).extracting(Reservation::getReservationId).contains(saved.getReservationId());
    }

    @Test
    @DisplayName("findByEventIdAndUserId returns single booking")
    void findByEventIdAndUserId() {
        User u = savedCustomer();
        Event ev = savedEvent();

        Reservation r = new Reservation();
        r.setUserId(u.getUserId());
        r.setEventId(ev.getEventId());
        r.setStatus(Reservation.Status.CONFIRMED);
        reservationRepository.save(r);

        Optional<Reservation> found =
                reservationRepository.findByEventIdAndUserId(ev.getEventId(), u.getUserId());
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(u.getUserId());
    }

    @Test
    @DisplayName("findByEventIdAndUserId is empty when user did not book")
    void findByEventIdAndUserIdMissing() {
        User u = savedCustomer();
        Event ev = savedEvent();

        assertThat(reservationRepository.findByEventIdAndUserId(ev.getEventId(), u.getUserId())).isEmpty();
    }

    @Test
    @DisplayName("existsByUserIdAndEventIdAndStatus reflects CONFIRMED booking")
    void existsByUserIdAndEventIdAndStatus() {
        User u = savedCustomer();
        Event ev = savedEvent();

        assertThat(reservationRepository.existsByUserIdAndEventIdAndStatus(
                u.getUserId(), ev.getEventId(), Reservation.Status.CONFIRMED)).isFalse();

        Reservation r = new Reservation();
        r.setUserId(u.getUserId());
        r.setEventId(ev.getEventId());
        r.setStatus(Reservation.Status.CONFIRMED);
        reservationRepository.save(r);

        assertThat(reservationRepository.existsByUserIdAndEventIdAndStatus(
                u.getUserId(), ev.getEventId(), Reservation.Status.CONFIRMED)).isTrue();
    }

    @Test
    @DisplayName("deleteById removes reservation")
    void deleteById() {
        User u = savedCustomer();
        Event ev = savedEvent();

        Reservation r = new Reservation();
        r.setUserId(u.getUserId());
        r.setEventId(ev.getEventId());
        r.setStatus(Reservation.Status.CONFIRMED);
        Reservation saved = reservationRepository.save(r);

        reservationRepository.deleteById(saved.getReservationId());

        assertThat(reservationRepository.findById(saved.getReservationId())).isEmpty();
    }
}
