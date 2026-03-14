package com.ticket.integration;

import com.ticket.model.Event;
import com.ticket.model.User;
import com.ticket.repository.EventRepository;
import com.ticket.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("EventRepository")
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private User organizer;

    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setName("Organizer");
        organizer.setEmail("org@test.com");
        organizer.setPassword("pass");
        organizer.setRole(User.Role.ADMIN);
        userRepository.save(organizer);

        Event jazz = new Event();
        jazz.setTitle("Jazz Night");
        jazz.setCategory("music");
        jazz.setLocation("Montreal");
        jazz.setEventDate(LocalDateTime.of(2026, 6, 15, 20, 0));
        jazz.setTotalSpots(100);
        jazz.setAvailableSpots(100);
        jazz.setStatus(Event.Status.ACTIVE);
        jazz.setOrganizer(organizer);
        eventRepository.save(jazz);

        Event sports = new Event();
        sports.setTitle("Raptors Game");
        sports.setCategory("sports");
        sports.setLocation("Toronto");
        sports.setEventDate(LocalDateTime.of(2026, 7, 10, 19, 0));
        sports.setTotalSpots(50);
        sports.setAvailableSpots(50);
        sports.setStatus(Event.Status.ACTIVE);
        sports.setOrganizer(organizer);
        eventRepository.save(sports);
    }

    // findByCategoryIgnoreCase

    @Nested
    @DisplayName("findByCategoryIgnoreCase")
    class FindByCategory {

        @Test
        @DisplayName("returns events matching the category")
        void found() {
            List<Event> result = eventRepository.findByCategoryIgnoreCase("music");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Jazz Night");
        }

        @Test
        @DisplayName("is case insensitive (edge case: uppercase input)")
        void caseInsensitive() {
            List<Event> result = eventRepository.findByCategoryIgnoreCase("MUSIC");
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("returns empty when no events match (edge case)")
        void noMatch() {
            List<Event> result = eventRepository.findByCategoryIgnoreCase("opera");
            assertThat(result).isEmpty();
        }
    }

    //findByLocationContainingIgnoreCase

    @Nested
    @DisplayName("findByLocationContainingIgnoreCase")
    class FindByLocation {

        @Test
        @DisplayName("returns events matching the location")
        void found() {
            List<Event> result = eventRepository.findByLocationContainingIgnoreCase("Montreal");
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Jazz Night");
        }

        @Test
        @DisplayName("is case insensitive (edge case: lowercase input)")
        void caseInsensitive() {
            List<Event> result = eventRepository.findByLocationContainingIgnoreCase("montreal");
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("returns empty when location does not match (edge case)")
        void noMatch() {
            List<Event> result = eventRepository.findByLocationContainingIgnoreCase("Vancouver");
            assertThat(result).isEmpty();
        }
    }

    // findByEventDateBetween

    @Nested
    @DisplayName("findByEventDateBetween")
    class FindByDate {

        @Test
        @DisplayName("returns events within the date range")
        void found() {
            LocalDateTime start = LocalDateTime.of(2026, 6, 15, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 6, 15, 23, 59);

            List<Event> result = eventRepository.findByEventDateBetween(start, end);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Jazz Night");
        }

        @Test
        @DisplayName("returns empty when no events fall in the range (edge case)")
        void noMatch() {
            LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2025, 1, 1, 23, 59);

            List<Event> result = eventRepository.findByEventDateBetween(start, end);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns multiple events when both fall in the range (edge case)")
        void multipleResults() {
            LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59);

            List<Event> result = eventRepository.findByEventDateBetween(start, end);
            assertThat(result).hasSize(2);
        }
    }
}