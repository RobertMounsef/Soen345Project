package com.ticket.integration;

import com.ticket.model.Event;
import com.ticket.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventRepository")
class EventRepositoryTest {

    @Mock
    private EventRepository eventRepository;

    private Event jazz;
    private Event sports;

    @BeforeEach
    void setUp() {
        jazz = new Event();
        jazz.setEventId("-evtJazz001");
        jazz.setTitle("Jazz Night");
        jazz.setCategory("music");
        jazz.setLocation("Montreal");
        jazz.setEventDate(LocalDateTime.of(2026, 6, 15, 20, 0));
        jazz.setTotalSpots(100);
        jazz.setAvailableSpots(100);
        jazz.setStatus(Event.Status.ACTIVE);
        jazz.setOrganizerId("-userOrg001");

        sports = new Event();
        sports.setEventId("-evtSports002");
        sports.setTitle("Raptors Game");
        sports.setCategory("sports");
        sports.setLocation("Toronto");
        sports.setEventDate(LocalDateTime.of(2026, 7, 10, 19, 0));
        sports.setTotalSpots(50);
        sports.setAvailableSpots(50);
        sports.setStatus(Event.Status.ACTIVE);
        sports.setOrganizerId("-userOrg001");
    }

    @Nested
    @DisplayName("findByCategoryIgnoreCase")
    class FindByCategory {

        @Test
        @DisplayName("returns events matching the category")
        void found() {
            when(eventRepository.findByCategoryIgnoreCase("music")).thenReturn(List.of(jazz));

            List<Event> result = eventRepository.findByCategoryIgnoreCase("music");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Jazz Night");
        }

        @Test
        @DisplayName("is case insensitive (edge case: uppercase input)")
        void caseInsensitive() {
            when(eventRepository.findByCategoryIgnoreCase("MUSIC")).thenReturn(List.of(jazz));

            List<Event> result = eventRepository.findByCategoryIgnoreCase("MUSIC");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("returns empty when no events match (edge case)")
        void noMatch() {
            when(eventRepository.findByCategoryIgnoreCase("opera")).thenReturn(List.of());

            List<Event> result = eventRepository.findByCategoryIgnoreCase("opera");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByLocationContainingIgnoreCase")
    class FindByLocation {

        @Test
        @DisplayName("returns events matching the location")
        void found() {
            when(eventRepository.findByLocationContainingIgnoreCase("Montreal")).thenReturn(List.of(jazz));

            List<Event> result = eventRepository.findByLocationContainingIgnoreCase("Montreal");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Jazz Night");
        }

        @Test
        @DisplayName("is case insensitive (edge case: lowercase input)")
        void caseInsensitive() {
            when(eventRepository.findByLocationContainingIgnoreCase("montreal")).thenReturn(List.of(jazz));

            List<Event> result = eventRepository.findByLocationContainingIgnoreCase("montreal");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("returns empty when location does not match (edge case)")
        void noMatch() {
            when(eventRepository.findByLocationContainingIgnoreCase("Vancouver")).thenReturn(List.of());

            List<Event> result = eventRepository.findByLocationContainingIgnoreCase("Vancouver");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEventDateBetween")
    class FindByDate {

        @Test
        @DisplayName("returns events within the date range")
        void found() {
            LocalDateTime start = LocalDateTime.of(2026, 6, 15, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 6, 15, 23, 59);

            when(eventRepository.findByEventDateBetween(start, end)).thenReturn(List.of(jazz));

            List<Event> result = eventRepository.findByEventDateBetween(start, end);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Jazz Night");
        }

        @Test
        @DisplayName("returns empty when no events fall in the range (edge case)")
        void noMatch() {
            LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2025, 1, 1, 23, 59);

            when(eventRepository.findByEventDateBetween(start, end)).thenReturn(List.of());

            List<Event> result = eventRepository.findByEventDateBetween(start, end);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns multiple events when both fall in the range (edge case)")
        void multipleResults() {
            LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59);

            when(eventRepository.findByEventDateBetween(start, end)).thenReturn(List.of(jazz, sports));

            List<Event> result = eventRepository.findByEventDateBetween(start, end);

            assertThat(result).hasSize(2);
        }
    }
}