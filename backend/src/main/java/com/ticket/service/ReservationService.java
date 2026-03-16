package com.ticket.service;

import com.ticket.model.Event;
import com.ticket.model.Reservation;
import com.ticket.model.User;
import com.ticket.repository.EventRepository;
import com.ticket.repository.ReservationRepository;
import com.ticket.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              EventRepository eventRepository,
                              EmailService emailService,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getReservationsByUserId(Integer userId) {
        return reservationRepository.findByUser_UserId(userId);
    }

    public List<Reservation> getReservationsByEventId(Integer eventId) {
        return reservationRepository.findByEvent_EventId(eventId);
    }

    public Optional<Reservation> getReservationByEventAndUser(Integer eventId, Integer userId) {
        return reservationRepository.findByEvent_EventIdAndUser_UserId(eventId, userId);
    }

    public Optional<Reservation> getReservationById(Integer id) {
        return reservationRepository.findById(id);
    }

    @Transactional
    public Reservation createReservation(Reservation reservation) {
        Integer userId = reservation.getUser().getUserId();
        Integer eventId = reservation.getEvent().getEventId();

        boolean alreadyReserved = reservationRepository
                .existsByUser_UserIdAndEvent_EventIdAndStatus(
                        userId,
                        eventId,
                        Reservation.Status.CONFIRMED
                );

        if (alreadyReserved) {
            throw new IllegalStateException("You have already reserved this event");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (event.getAvailableSpots() <= 0) {
            throw new IllegalStateException("No available spots for this event");
        }

        event.setAvailableSpots(event.getAvailableSpots() - 1);
        eventRepository.save(event);

        Reservation saved = reservationRepository.save(reservation);

        User user = userRepository.findById(saved.getUser().getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            try {
                emailService.sendReservationConfirmation(
                        user.getEmail(),
                        user.getName(),
                        event.getTitle()
                );
            } catch (Exception e) {
                System.out.println("Email failed, but reservation was created: " + e.getMessage());
            }
        }

        return saved;
    }

    @Transactional
    public void deleteReservation(Integer id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        Event event = reservation.getEvent();
        event.setAvailableSpots(event.getAvailableSpots() + 1);
        eventRepository.save(event);

        String email = reservation.getUser().getEmail();
        String userName = reservation.getUser().getName();
        String eventTitle = event.getTitle();

        if (email != null && !email.isBlank()) {
            try {
                emailService.sendReservationCancellation(email, userName, eventTitle);
            } catch (Exception e) {
                System.out.println("Cancellation email failed, but reservation was deleted: " + e.getMessage());
            }
        }

        reservationRepository.deleteById(id);
    }
}