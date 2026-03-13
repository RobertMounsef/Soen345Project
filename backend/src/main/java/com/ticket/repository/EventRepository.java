package com.ticket.repository;

import com.ticket.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByOrganizer_UserId(Integer organizerId);

    List<Event> findByCategoryIgnoreCase(String category);

    List<Event> findByLocationContainingIgnoreCase(String location);

    List<Event> findByEventDateBetween(LocalDateTime start, LocalDateTime end);
}