package com.ticket.integration;

import com.ticket.model.Event;
import com.ticket.model.Reservation;
import com.ticket.model.User;
import com.ticket.repository.EventRepository;
import com.ticket.repository.ReservationRepository;
import com.ticket.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ReservationRepository")
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private User alice;
    private Event jazzNight;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setName("Alice");
        alice.setEmail("alice@test.com");
        alice.setPassword("secret");
        alice.setRole(User.Role.CUSTOMER);
        userRepository.save(alice);

        jazzNight = new Event();
        jazzNight.setTitle("Jazz Night");
        jazzNight.setCategory("music");
        jazzNight.setLocation("Montreal");
        jazzNight.setEventDate(LocalDateTime.of(2026, 6, 15, 20, 0));
        jazzNight.setTotalSpots(100);
        jazzNight.setAvailableSpots(100);
        jazzNight.setStatus(Event.Status.ACTIVE);
        jazzNight.setOrganizer(alice);
        eventRepository.save(jazzNight);

        Reservation reservation = new Reservation();
        reservation.setUser(alice);
        reservation.setEvent(jazzNight);
        reservation.setStatus(Reservation.Status.CONFIRMED);
        reservationRepository.save(reservation);
    }

    // findByUser_UserId

    @Nested
    @DisplayName("findByUser_UserId")
    class FindByUser {

        @Test
        @DisplayName("returns reservations for the given user")
        void found() {
            List<Reservation> result = reservationRepository.findByUser_UserId(alice.getUserId());
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEvent().getTitle()).isEqualTo("Jazz Night");
        }

        @Test
        @DisplayName("returns empty when user has no reservations (edge case)")
        void noReservations() {
            User bob = new User();
            bob.setName("Bob");
            bob.setEmail("bob@test.com");
            bob.setPassword("pass");
            bob.setRole(User.Role.CUSTOMER);
            userRepository.save(bob);

            List<Reservation> result = reservationRepository.findByUser_UserId(bob.getUserId());
            assertThat(result).isEmpty();
        }
    }

    // findByEvent_EventId

    @Nested
    @DisplayName("findByEvent_EventId")
    class FindByEvent {

        @Test
        @DisplayName("returns reservations for the given event")
        void found() {
            List<Reservation> result = reservationRepository.findByEvent_EventId(jazzNight.getEventId());
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("returns empty when event has no reservations (edge case)")
        void noReservations() {
            Event other = new Event();
            other.setTitle("Blues Night");
            other.setCategory("music");
            other.setLocation("Toronto");
            other.setEventDate(LocalDateTime.of(2026, 7, 10, 19, 0));
            other.setTotalSpots(50);
            other.setAvailableSpots(50);
            other.setStatus(Event.Status.ACTIVE);
            other.setOrganizer(alice);
            eventRepository.save(other);

            List<Reservation> result = reservationRepository.findByEvent_EventId(other.getEventId());
            assertThat(result).isEmpty();
        }
    }

    // findByEvent_EventIdAndUser_UserId 

    @Nested
    @DisplayName("findByEvent_EventIdAndUser_UserId")
    class FindByEventAndUser {

        @Test
        @DisplayName("returns reservation when user has booked the event")
        void found() {
            Optional<Reservation> result = reservationRepository
                    .findByEvent_EventIdAndUser_UserId(jazzNight.getEventId(), alice.getUserId());
            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("returns empty when user has not booked the event (edge case)")
        void notBooked() {
            User bob = new User();
            bob.setName("Bob");
            bob.setEmail("bob@test.com");
            bob.setPassword("pass");
            bob.setRole(User.Role.CUSTOMER);
            userRepository.save(bob);

            Optional<Reservation> result = reservationRepository
                    .findByEvent_EventIdAndUser_UserId(jazzNight.getEventId(), bob.getUserId());
            assertThat(result).isEmpty();
        }
    }
}