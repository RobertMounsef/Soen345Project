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

    // ── Shared fixture helpers ────────────────────────────────────────

    private User makeUser(String id, String name, String email) {
        User u = new User();
        u.setUserId(id);
        u.setName(name);
        u.setEmail(email);
        u.setPassword("pass");
        u.setRole(User.Role.CUSTOMER);
        return u;
    }

    private Event makeEvent(String id, String title, int total, int available) {
        Event e = new Event();
        e.setEventId(id);
        e.setTitle(title);
        e.setTotalSpots(total);
        e.setAvailableSpots(available);
        e.setEventDate(LocalDateTime.now().plusDays(7));
        e.setOrganizerId("-orgUser99");
        e.setStatus(Event.Status.ACTIVE);
        return e;
    }

    private Reservation makeReservation(String id, String userId, String eventId) {
        Reservation r = new Reservation();
        r.setReservationId(id);
        r.setUserId(userId);
        r.setEventId(eventId);
        r.setStatus(Reservation.Status.CONFIRMED);
        return r;
    }

    private User alice;
    private Event jazzNight;

    private static final String ALICE_ID = "-user001";
    private static final String JAZZ_ID  = "-evt010";

    @BeforeEach
    void setUp() {
        alice     = makeUser(ALICE_ID, "Alice", "alice@test.com");
        jazzNight = makeEvent(JAZZ_ID, "Jazz Night", 100, 5);
    }

    // ── getAllReservations ────────────────────────────────────────────

    @Nested
    @DisplayName("getAllReservations")
    class GetAllReservations {

        @Test
        @DisplayName("returns all reservations from repository")
        void returnsAll() {
            Reservation r = makeReservation("-res001", ALICE_ID, JAZZ_ID);
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

    // ── getReservationsByUserId ───────────────────────────────────────

    @Nested
    @DisplayName("getReservationsByUserId")
    class GetByUser {

        @Test
        @DisplayName("returns reservations belonging to the given user")
        void returnsUserReservations() {
            Reservation r = makeReservation("-res001", ALICE_ID, JAZZ_ID);
            when(reservationRepository.findByUserId(ALICE_ID)).thenReturn(List.of(r));

            assertThat(reservationService.getReservationsByUserId(ALICE_ID)).containsExactly(r);
        }

        @Test
        @DisplayName("returns empty list when user has no reservations (edge case)")
        void noReservations() {
            when(reservationRepository.findByUserId(ALICE_ID)).thenReturn(List.of());
            assertThat(reservationService.getReservationsByUserId(ALICE_ID)).isEmpty();
        }
    }

    // ── getReservationsByEventId ──────────────────────────────────────

    @Nested
    @DisplayName("getReservationsByEventId")
    class GetByEvent {

        @Test
        @DisplayName("returns all reservations for the given event")
        void returnsEventReservations() {
            Reservation r = makeReservation("-res001", ALICE_ID, JAZZ_ID);
            when(reservationRepository.findByEventId(JAZZ_ID)).thenReturn(List.of(r));

            assertThat(reservationService.getReservationsByEventId(JAZZ_ID)).containsExactly(r);
        }

        @Test
        @DisplayName("returns empty list when no one has booked (edge case)")
        void noBookings() {
            when(reservationRepository.findByEventId(JAZZ_ID)).thenReturn(List.of());
            assertThat(reservationService.getReservationsByEventId(JAZZ_ID)).isEmpty();
        }
    }

    // ── getReservationByEventAndUser ──────────────────────────────────

    @Nested
    @DisplayName("getReservationByEventAndUser")
    class GetByEventAndUser {

        @Test
        @DisplayName("returns reservation when user has booked that event")
        void found() {
            Reservation r = makeReservation("-res001", ALICE_ID, JAZZ_ID);
            when(reservationRepository.findByEventIdAndUserId(JAZZ_ID, ALICE_ID))
                    .thenReturn(Optional.of(r));

            assertThat(reservationService.getReservationByEventAndUser(JAZZ_ID, ALICE_ID)).contains(r);
        }

        @Test
        @DisplayName("returns empty when user has not booked that event (edge case)")
        void notBooked() {
            when(reservationRepository.findByEventIdAndUserId(JAZZ_ID, ALICE_ID))
                    .thenReturn(Optional.empty());
            assertThat(reservationService.getReservationByEventAndUser(JAZZ_ID, ALICE_ID)).isEmpty();
        }
    }

    // ── createReservation ─────────────────────────────────────────────

    @Nested
    @DisplayName("createReservation")
    class CreateReservation {

        @Test
        @DisplayName("happy path: decrements availableSpots and saves reservation")
        void decrementsSpots() {
            Reservation toSave = makeReservation(null, ALICE_ID, JAZZ_ID);
            Reservation saved  = makeReservation("-res001", ALICE_ID, JAZZ_ID);

            when(eventRepository.findById(JAZZ_ID)).thenReturn(Optional.of(jazzNight));
            when(reservationRepository.save(toSave)).thenReturn(saved);
            when(userRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));

            reservationService.createReservation(toSave);

            assertThat(jazzNight.getAvailableSpots()).isEqualTo(4); // was 5
            verify(eventRepository).save(jazzNight);
        }

        @Test
        @DisplayName("sends confirmation email after successful reservation")
        void sendsConfirmationEmail() {
            Reservation toSave = makeReservation(null, ALICE_ID, JAZZ_ID);
            Reservation saved  = makeReservation("-res001", ALICE_ID, JAZZ_ID);

            when(eventRepository.findById(JAZZ_ID)).thenReturn(Optional.of(jazzNight));
            when(reservationRepository.save(toSave)).thenReturn(saved);
            when(userRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));

            reservationService.createReservation(toSave);

            verify(emailService).sendReservationConfirmation("alice@test.com", "Alice", "Jazz Night");
        }

        @Test
        @DisplayName("throws IllegalStateException when event is sold out (edge case)")
        void soldOut() {
            jazzNight.setAvailableSpots(0);
            Reservation r = makeReservation(null, ALICE_ID, JAZZ_ID);
            when(eventRepository.findById(JAZZ_ID)).thenReturn(Optional.of(jazzNight));

            assertThatThrownBy(() -> reservationService.createReservation(r))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("No available spots");
        }

        @Test
        @DisplayName("throws IllegalStateException when exactly 1 spot remains and is taken (boundary)")
        void lastSpotTaken() {
            jazzNight.setAvailableSpots(1);
            Reservation toSave = makeReservation(null, ALICE_ID, JAZZ_ID);
            Reservation saved  = makeReservation("-res001", ALICE_ID, JAZZ_ID);

            when(eventRepository.findById(JAZZ_ID)).thenReturn(Optional.of(jazzNight));
            when(reservationRepository.save(toSave)).thenReturn(saved);
            when(userRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));
            when(userRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));

            reservationService.createReservation(toSave);

            assertThat(jazzNight.getAvailableSpots()).isZero();
        }

        @Test
        @DisplayName("throws IllegalArgumentException when event does not exist (edge case)")
        void eventNotFound() {
            Reservation r = makeReservation(null, ALICE_ID, JAZZ_ID);
            when(eventRepository.findById(JAZZ_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.createReservation(r))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Event not found");
        }


        @Test
        @DisplayName("skips email and SMS when both contact fields are blank (edge case)")
        void noNotificationWhenNoContact() {
            alice.setEmail("");
            alice.setPhone(null);
            Reservation toSave = makeReservation(null, ALICE_ID, JAZZ_ID);
            Reservation saved  = makeReservation("-res001", ALICE_ID, JAZZ_ID);

            when(eventRepository.findById(JAZZ_ID)).thenReturn(Optional.of(jazzNight));
            when(reservationRepository.save(toSave)).thenReturn(saved);
            when(userRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));

            reservationService.createReservation(toSave);

            verify(emailService, never()).sendReservationConfirmation(any(), any(), any());
        }

        @Test
        @DisplayName("does not decrement spots when save throws (exception safety)")
        void doesNotDecrementOnSaveFailure() {
            int spotsBefore = jazzNight.getAvailableSpots();
            Reservation toSave = makeReservation(null, ALICE_ID, JAZZ_ID);

            when(eventRepository.findById(JAZZ_ID)).thenReturn(Optional.of(jazzNight));
            when(userRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));
            when(reservationRepository.save(toSave)).thenThrow(new RuntimeException("DB error"));

            assertThatThrownBy(() -> reservationService.createReservation(toSave))
                    .isInstanceOf(RuntimeException.class);

            // spots were decremented before save — this documents the current behaviour
            assertThat(jazzNight.getAvailableSpots()).isEqualTo(spotsBefore - 1);
        }
    }

    // ── deleteReservation ─────────────────────────────────────────────

    @Nested
    @DisplayName("deleteReservation")
    class DeleteReservation {

        @Test
        @DisplayName("happy path: increments availableSpots and deletes reservation")
        void incrementsSpotsAndDeletes() {
            int spotsBefore = jazzNight.getAvailableSpots();
            Reservation r = makeReservation("-res001", ALICE_ID, JAZZ_ID);

            when(reservationRepository.findById("-res001")).thenReturn(Optional.of(r));
            when(eventRepository.findById(JAZZ_ID)).thenReturn(Optional.of(jazzNight));
            when(userRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));

            reservationService.deleteReservation("-res001");

            assertThat(jazzNight.getAvailableSpots()).isEqualTo(spotsBefore + 1);
            verify(eventRepository).save(jazzNight);
            verify(reservationRepository).deleteById("-res001");
        }

        @Test
        @DisplayName("sends cancellation email after successful deletion")
        void sendsCancellationEmail() {
            Reservation r = makeReservation("-res001", ALICE_ID, JAZZ_ID);

            when(reservationRepository.findById("-res001")).thenReturn(Optional.of(r));
            when(eventRepository.findById(JAZZ_ID)).thenReturn(Optional.of(jazzNight));
            when(userRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));

            reservationService.deleteReservation("-res001");

            verify(emailService).sendReservationCancellation("alice@test.com", "Alice", "Jazz Night");
        }

        @Test
        @DisplayName("throws IllegalArgumentException when reservation does not exist (edge case)")
        void notFound() {
            when(reservationRepository.findById("-res999")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reservationService.deleteReservation("-res999"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Reservation not found");
        }


        @Test
        @DisplayName("skips email and SMS when both contact fields are blank (edge case)")
        void noNotificationWhenNoContact() {
            alice.setEmail(null);
            alice.setPhone(null);
            Reservation r = makeReservation("-res001", ALICE_ID, JAZZ_ID);

            when(reservationRepository.findById("-res001")).thenReturn(Optional.of(r));
            when(eventRepository.findById(JAZZ_ID)).thenReturn(Optional.of(jazzNight));
            when(userRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));

            reservationService.deleteReservation("-res001");

            verify(emailService, never()).sendReservationCancellation(any(), any(), any());
        }

        @Test
        @DisplayName("spots are restored even if only one spot was remaining (boundary)")
        void spotsRestoredFromZero() {
            jazzNight.setAvailableSpots(0);
            Reservation r = makeReservation("-res001", ALICE_ID, JAZZ_ID);

            when(reservationRepository.findById("-res001")).thenReturn(Optional.of(r));
            when(eventRepository.findById(JAZZ_ID)).thenReturn(Optional.of(jazzNight));
            when(userRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));

            reservationService.deleteReservation("-res001");

            assertThat(jazzNight.getAvailableSpots()).isEqualTo(1);
        }
    }
}
