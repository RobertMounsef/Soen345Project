package com.ticket.repository;

import com.ticket.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByUser_UserId(Integer userId);

    List<Reservation> findByEvent_EventId(Integer eventId);

    Optional<Reservation> findByEvent_EventIdAndUser_UserId(Integer eventId, Integer userId);
}