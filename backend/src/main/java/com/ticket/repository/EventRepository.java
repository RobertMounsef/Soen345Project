package com.ticket.repository;

import com.ticket.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByOrganizer_UserId(Integer organizerId);
}