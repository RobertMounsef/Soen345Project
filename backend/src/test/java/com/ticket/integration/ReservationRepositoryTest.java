package com.ticket.integration;

import com.ticket.model.Event;
import com.ticket.model.Reservation;
import com.ticket.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationRepository")
class ReservationRepositoryTest {

    @Mock
    private ReservationRepository reservationRepository;

    private static final String ALICE_ID = "-userAlice001";
    private static final String JAZZ_ID  = "-evtJazz001";

    private Reservation jazzReservation;

    @BeforeEach
    void setUp() {
        jazzReservation = new Reservation();
        jazzReservation.setReservationId("-res001");
        jazzReservation.setUserId(ALICE_ID);
        jazzReservation.setEventId(JAZZ_ID);
        jazzReservation.setReservationDate(LocalDateTime.of(2026, 5, 1, 10, 0));
        jazzReservation.setStatus(Reservation.Status.CONFIRMED);
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUser {

        @Test
        @DisplayName("returns reservations for the given user")
        void found() {
            when(reservationRepository.findByUserId(ALICE_ID)).thenReturn(List.of(jazzReservation));

            List<Reservation> result = reservationRepository.findByUserId(ALICE_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEventId()).isEqualTo(JAZZ_ID);
        }

        @Test
        @DisplayName("returns empty when user has no reservations (edge case)")
        void noReservations() {
            String bobId = "-userBob002";
            when(reservationRepository.findByUserId(bobId)).thenReturn(List.of());

            List<Reservation> result = reservationRepository.findByUserId(bobId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEventId")
    class FindByEvent {

        @Test
        @DisplayName("returns reservations for the given event")
        void found() {
            when(reservationRepository.findByEventId(JAZZ_ID)).thenReturn(List.of(jazzReservation));

            List<Reservation> result = reservationRepository.findByEventId(JAZZ_ID);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("returns empty when event has no reservations (edge case)")
        void noReservations() {
            String bluesId = "-evtBlues003";
            when(reservationRepository.findByEventId(bluesId)).thenReturn(List.of());

            List<Reservation> result = reservationRepository.findByEventId(bluesId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEventIdAndUserId")
    class FindByEventAndUser {

        @Test
        @DisplayName("returns reservation when user has booked the event")
        void found() {
            when(reservationRepository.findByEventIdAndUserId(JAZZ_ID, ALICE_ID))
                    .thenReturn(Optional.of(jazzReservation));

            Optional<Reservation> result = reservationRepository.findByEventIdAndUserId(JAZZ_ID, ALICE_ID);

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("returns empty when user has not booked the event (edge case)")
        void notBooked() {
            String bobId = "-userBob002";
            when(reservationRepository.findByEventIdAndUserId(JAZZ_ID, bobId))
                    .thenReturn(Optional.empty());

            Optional<Reservation> result = reservationRepository.findByEventIdAndUserId(JAZZ_ID, bobId);

            assertThat(result).isEmpty();
        }
    }
}