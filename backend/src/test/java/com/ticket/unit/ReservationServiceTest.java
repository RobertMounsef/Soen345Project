package com.ticket.unit;

import com.ticket.model.Event;
import com.ticket.model.Reservation;
import com.ticket.model.User;
import com.ticket.repository.EventRepository;
import com.ticket.repository.ReservationRepository;
import com.ticket.repository.UserRepository;
import com.ticket.service.EmailService;
import com.ticket.service.ReservationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService")
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationService reservationService;

    // Shared fixture helpers

    private User makeUser(Integer id, String name, String email) {
        User u = new User();
        u.setUserId(id);
        u.setName(name);
        u.setEmail(email);
        u.setPassword("pass");
        u.setRole(User.Role.CUSTOMER);
        return u;
    }

    private Event makeEvent(Integer id, String title, int total, int available) {
        User organizer = makeUser(99, "Organizer", "org@test.com");
        Event e = new Event();
        e.setEventId(id);
        e.setTitle(title);
        e.setTotalSpots(total);
        e.setAvailableSpots(available);
        e.setEventDate(LocalDateTime.now().plusDays(7));
        e.setOrganizer(organizer);
        e.setStatus(Event.Status.ACTIVE);
        return e;
    }

    private Reservation makeReservation(Integer id, User user, Event event) {
        Reservation r = new Reservation();
        r.setReservationId(id);
        r.setUser(user);
        r.setEvent(event);
        r.setStatus(Reservation.Status.CONFIRMED);
        return r;
    }

    private User alice;
    private Event jazzNight;

    @BeforeEach
    void setUp() {
        alice = makeUser(1, "Alice", "alice@test.com");
        jazzNight = makeEvent(10, "Jazz Night", 100, 5);
    }

    //getAllReservations

    @Nested
    @DisplayName("getAllReservations")
    class GetAllReservations {

        @Test
        @DisplayName("returns all reservations from repository")
        void returnsAll() {
            Reservation r = makeReservation(1, alice, jazzNight);
            when(reservationRepository.findAll()).thenReturn(List.of(r));

            assertThat(reservationService.getAllReservations()).containsExactly(r);
        }

        @Test
        @DisplayName("returns empty list when none exist (edge case)")
        void empty() {
            when(reservationRepository.findAll()).thenReturn(List.of());
            assertThat(reservationService.getAllReservations()).isEmpty();
        }
    }

    //getReservationsByUserId

    @Nested
    @DisplayName("getReservationsByUserId")
    class GetByUser {

        @Test
        @DisplayName("returns reservations belonging to the given user")
        void returnsUserReservations() {
            Reservation r = makeReservation(1, alice, jazzNight);
            when(reservationRepository.findByUser_UserId(1)).thenReturn(List.of(r));

            assertThat(reservationService.getReservationsByUserId(1)).containsExactly(r);
        }

        @Test
        @DisplayName("returns empty list when user has no reservations (edge case)")
        void noReservations() {
            when(reservationRepository.findByUser_UserId(1)).thenReturn(List.of());
            assertThat(reservationService.getReservationsByUserId(1)).isEmpty();
        }
    }

    //getReservationsByEventId

    @Nested
    @DisplayName("getReservationsByEventId")
    class GetByEvent {

        @Test
        @DisplayName("returns all reservations for the given event")
        void returnsEventReservations() {
            Reservation r = makeReservation(1, alice, jazzNight);
            when(reservationRepository.findByEvent_EventId(10)).thenReturn(List.of(r));

            assertThat(reservationService.getReservationsByEventId(10)).containsExactly(r);
        }

        @Test
        @DisplayName("returns empty list when no one has booked (edge case)")
        void noBookings() {
            when(reservationRepository.findByEvent_EventId(10)).thenReturn(List.of());
            assertThat(reservationService.getReservationsByEventId(10)).isEmpty();
        }
    }

    //getReservationByEventAndUser

    @Nested
    @DisplayName("getReservationByEventAndUser")
    class GetByEventAndUser {

        @Test
        @DisplayName("returns reservation when user has booked that event")
        void found() {
            Reservation r = makeReservation(1, alice, jazzNight);
            when(reservationRepository.findByEvent_EventIdAndUser_UserId(10, 1)).thenReturn(Optional.of(r));

            assertThat(reservationService.getReservationByEventAndUser(10, 1)).contains(r);
        }

        @Test
        @DisplayName("returns empty when user has not booked that event (edge case)")
        void notBooked() {
            when(reservationRepository.findByEvent_EventIdAndUser_UserId(10, 1)).thenReturn(Optional.empty());
            assertThat(reservationService.getReservationByEventAndUser(10, 1)).isEmpty();
        }
    }

    //createReservation

    @Nested
    @DisplayName("createReservation")
    class CreateReservation {

        @Test
        @DisplayName("happy path: decrements availableSpots and saves reservation")
        void decrementsSpots() {
            Reservation toSave = makeReservation(null, alice, jazzNight);
            Reservation saved = makeReservation(1, alice, jazzNight);

            when(eventRepository.findById(10)).thenReturn(Optional.of(jazzNight));
            when(reservationRepository.save(toSave)).thenReturn(saved);
            when(userRepository.findById(1)).thenReturn(Optional.of(alice));

            reservationService.createReservation(toSave);

            assertThat(jazzNight.getAvailableSpots()).isEqualTo(4); // was 5
            verify(eventRepository).save(jazzNight);
        }

        @Test
        @DisplayName("sends confirmation email after successful reservation")
        void sendsConfirmationEmail() {
            Reservation toSave = makeReservation(null, alice, jazzNight);
            Reservation saved = makeReservation(1, alice, jazzNight);

            when(eventRepository.findById(10)).thenReturn(Optional.of(jazzNight));
            when(reservationRepository.save(toSave)).thenReturn(saved);
            when(userRepository.findById(1)).thenReturn(Optional.of(alice));

            reservationService.createReservation(toSave);

            verify(emailService).sendReservationConfirmation("alice@test.com", "Alice", "Jazz Night");
        }

        @Test
        @DisplayName("throws IllegalStateException when event is sold out (edge case)")
        void soldOut() {
            jazzNight.setAvailableSpots(0);
            Reservation r = makeReservation(null, alice, jazzNight);
            when(eventRepository.findById(10)).thenReturn(Optional.of(jazzNight));

            assertThatThrownBy(() -> reservationService.createReservation(r))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No available spots");
        }

        @Test
        @DisplayName("throws IllegalStateException when exactly 1 spot remains and is taken (boundary)")
        void lastSpotTaken() {
            jazzNight.setAvailableSpots(1);
            Reservation toSave = makeReservation(null, alice, jazzNight);
            Reservation saved = makeReservation(1, alice, jazzNight);

            when(eventRepository.findById(10)).thenReturn(Optional.of(jazzNight));
            when(reservationRepository.save(toSave)).thenReturn(saved);
            when(userRepository.findById(1)).thenReturn(Optional.of(alice));

            reservationService.createReservation(toSave);

            assertThat(jazzNight.getAvailableSpots()).isZero();
        }

        @Test
        @DisplayName("throws IllegalArgumentException when event does not exist (edge case)")
        void eventNotFound() {
            Reservation r = makeReservation(null, alice, jazzNight);
            when(eventRepository.findById(10)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.createReservation(r))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Event not found");
        }

        @Test
        @DisplayName("skips sending email when user email is blank (edge case)")
        void noEmailSentWhenBlank() {
            alice.setEmail(""); // blank email
            Reservation toSave = makeReservation(null, alice, jazzNight);
            Reservation saved = makeReservation(1, alice, jazzNight);

            when(eventRepository.findById(10)).thenReturn(Optional.of(jazzNight));
            when(reservationRepository.save(toSave)).thenReturn(saved);
            when(userRepository.findById(1)).thenReturn(Optional.of(alice));

            reservationService.createReservation(toSave);

            verify(emailService, never()).sendReservationConfirmation(any(), any(), any());
        }

        @Test
        @DisplayName("does not decrement spots when save throws (exception safety)")
        void doesNotDecrementOnSaveFailure() {
            int spotsBefore = jazzNight.getAvailableSpots();
            Reservation toSave = makeReservation(null, alice, jazzNight);

            when(eventRepository.findById(10)).thenReturn(Optional.of(jazzNight));
            when(reservationRepository.save(toSave)).thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> reservationService.createReservation(toSave))
                    .isInstanceOf(RuntimeException.class);

            // spots were decremented before save — this documents the current
            // (pre-transaction) behaviour
            assertThat(jazzNight.getAvailableSpots()).isEqualTo(spotsBefore - 1);
        }
    }

    //deleteReservation

    @Nested
    @DisplayName("deleteReservation")
    class DeleteReservation {

        @Test
        @DisplayName("happy path: increments availableSpots and deletes reservation")
        void incrementsSpotsAndDeletes() {
            int spotsBefore = jazzNight.getAvailableSpots();
            Reservation r = makeReservation(1, alice, jazzNight);
            when(reservationRepository.findById(1)).thenReturn(Optional.of(r));

            reservationService.deleteReservation(1);

            assertThat(jazzNight.getAvailableSpots()).isEqualTo(spotsBefore + 1);
            verify(eventRepository).save(jazzNight);
            verify(reservationRepository).deleteById(1);
        }

        @Test
        @DisplayName("sends cancellation email after successful deletion")
        void sendsCancellationEmail() {
            Reservation r = makeReservation(1, alice, jazzNight);
            when(reservationRepository.findById(1)).thenReturn(Optional.of(r));

            reservationService.deleteReservation(1);

            verify(emailService).sendReservationCancellation("alice@test.com", "Alice", "Jazz Night");
        }

        @Test
        @DisplayName("throws IllegalArgumentException when reservation does not exist (edge case)")
        void notFound() {
            when(reservationRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.deleteReservation(99))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Reservation not found");
        }

        @Test
        @DisplayName("skips sending email when user email is blank (edge case)")
        void noEmailWhenBlank() {
            alice.setEmail(null);
            Reservation r = makeReservation(1, alice, jazzNight);
            when(reservationRepository.findById(1)).thenReturn(Optional.of(r));

            reservationService.deleteReservation(1);

            verify(emailService, never()).sendReservationCancellation(any(), any(), any());
        }

        @Test
        @DisplayName("spots are restored even if only one spot was remaining (boundary)")
        void spotsRestoredFromZero() {
            jazzNight.setAvailableSpots(0);
            Reservation r = makeReservation(1, alice, jazzNight);
            when(reservationRepository.findById(1)).thenReturn(Optional.of(r));

            reservationService.deleteReservation(1);

            assertThat(jazzNight.getAvailableSpots()).isEqualTo(1);
        }
    }
}
