package com.ticket.service;

import com.ticket.model.Event;
import com.ticket.model.User;
import com.ticket.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    // ── Shared test fixture ─────────────────────────────────────────
    private Event makeEvent(Integer id, String title, String category, String location,
            int total, int available, LocalDateTime date) {
        User organizer = new User();
        organizer.setUserId(10);

        Event e = new Event();
        e.setEventId(id);
        e.setTitle(title);
        e.setCategory(category);
        e.setLocation(location);
        e.setTotalSpots(total);
        e.setAvailableSpots(available);
        e.setEventDate(date);
        e.setOrganizer(organizer);
        e.setStatus(Event.Status.ACTIVE);
        return e;
    }

    private Event jazz;

    @BeforeEach
    void setUp() {
        jazz = makeEvent(1, "Jazz Night", "music", "Montreal", 100, 100,
                LocalDateTime.of(2025, 4, 20, 19, 0));
    }

    // ── getAllEvents ───────────────────────────────────────────────

    @Nested
    @DisplayName("getAllEvents")
    class GetAllEvents {

        @Test
        @DisplayName("returns all events from repository")
        void returnsAll() {
            when(eventRepository.findAll()).thenReturn(List.of(jazz));
            assertThat(eventService.getAllEvents()).containsExactly(jazz);
        }

        @Test
        @DisplayName("returns empty list when no events exist (edge case)")
        void empty() {
            when(eventRepository.findAll()).thenReturn(List.of());
            assertThat(eventService.getAllEvents()).isEmpty();
        }
    }

    // getEventById 

    @Nested
    @DisplayName("getEventById")
    class GetEventById {

        @Test
        @DisplayName("returns event when found")
        void found() {
            when(eventRepository.findById(1)).thenReturn(Optional.of(jazz));
            assertThat(eventService.getEventById(1)).contains(jazz);
        }

        @Test
        @DisplayName("returns empty when event does not exist (edge case)")
        void notFound() {
            when(eventRepository.findById(99)).thenReturn(Optional.empty());
            assertThat(eventService.getEventById(99)).isEmpty();
        }
    }

    // createEvent

    @Nested
    @DisplayName("createEvent")
    class CreateEvent {

        @Test
        @DisplayName("auto-sets availableSpots = totalSpots before saving")
        void setsAvailableSpots() {
            jazz.setAvailableSpots(0); // deliberately wrong — service must fix it
            when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

            Event result = eventService.createEvent(jazz);

            assertThat(result.getAvailableSpots()).isEqualTo(jazz.getTotalSpots());
        }

        @Test
        @DisplayName("delegates save to repository")
        void savesEvent() {
            when(eventRepository.save(jazz)).thenReturn(jazz);
            eventService.createEvent(jazz);
            verify(eventRepository).save(jazz);
        }

        @Test
        @DisplayName("works correctly when totalSpots is 0 (edge case: zero-capacity event)")
        void zeroCapacity() {
            jazz.setTotalSpots(0);
            when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

            Event result = eventService.createEvent(jazz);

            assertThat(result.getAvailableSpots()).isZero();
        }
    }

    //updateEvent 

    @Nested
    @DisplayName("updateEvent")
    class UpdateEvent {

        @Test
        @DisplayName("updates all fields from the payload")
        void updatesAllFields() {
            Event updated = makeEvent(null, "Blues Night", "blues", "Quebec City",
                    200, 150, LocalDateTime.of(2025, 5, 1, 20, 0));
            updated.setStatus(Event.Status.INACTIVE);

            when(eventRepository.findById(1)).thenReturn(Optional.of(jazz));
            when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

            Event result = eventService.updateEvent(1, updated);

            assertThat(result.getTitle()).isEqualTo("Blues Night");
            assertThat(result.getCategory()).isEqualTo("blues");
            assertThat(result.getLocation()).isEqualTo("Quebec City");
            assertThat(result.getTotalSpots()).isEqualTo(200);
            assertThat(result.getAvailableSpots()).isEqualTo(150);
            assertThat(result.getStatus()).isEqualTo(Event.Status.INACTIVE);
        }

        @Test
        @DisplayName("throws when event id does not exist (edge case)")
        void eventNotFound() {
            when(eventRepository.findById(99)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> eventService.updateEvent(99, jazz))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }

    //deleteEvent

    @Nested
    @DisplayName("deleteEvent")
    class DeleteEvent {

        @Test
        @DisplayName("delegates to repository deleteById")
        void delegates() {
            eventService.deleteEvent(1);
            verify(eventRepository).deleteById(1);
        }
    }

    //searchByCategory

    @Nested
    @DisplayName("searchByCategory")
    class SearchByCategory {

        @Test
        @DisplayName("returns events matching the category")
        void matchingCategory() {
            when(eventRepository.findByCategoryIgnoreCase("music")).thenReturn(List.of(jazz));
            assertThat(eventService.searchByCategory("music")).containsExactly(jazz);
        }

        @Test
        @DisplayName("returns empty list when no events match (edge case)")
        void noMatch() {
            when(eventRepository.findByCategoryIgnoreCase("opera")).thenReturn(List.of());
            assertThat(eventService.searchByCategory("opera")).isEmpty();
        }

        @Test
        @DisplayName("passes term as-is to repository (case handling is repository's job)")
        void passesThroughSearchTerm() {
            eventService.searchByCategory("MUSIC");
            verify(eventRepository).findByCategoryIgnoreCase("MUSIC");
        }
    }

    //searchByLocation

    @Nested
    @DisplayName("searchByLocation")
    class SearchByLocation {

        @Test
        @DisplayName("returns events matching the location")
        void matchingLocation() {
            when(eventRepository.findByLocationContainingIgnoreCase("Montreal")).thenReturn(List.of(jazz));
            assertThat(eventService.searchByLocation("Montreal")).containsExactly(jazz);
        }

        @Test
        @DisplayName("returns empty list when location does not match (edge case)")
        void noMatch() {
            when(eventRepository.findByLocationContainingIgnoreCase("Toronto")).thenReturn(List.of());
            assertThat(eventService.searchByLocation("Toronto")).isEmpty();
        }
    }

    //searchByDate

    @Nested
    @DisplayName("searchByDate")
    class SearchByDate {

        @Test
        @DisplayName("expands LocalDate into full-day window and queries repository")
        void expandsToFullDay() {
            LocalDate date = LocalDate.of(2025, 4, 20);
            LocalDateTime expectedStart = date.atStartOfDay();
            LocalDateTime expectedEnd = date.atTime(23, 59, 59);

            when(eventRepository.findByEventDateBetween(expectedStart, expectedEnd))
                    .thenReturn(List.of(jazz));

            List<Event> result = eventService.searchByDate(date);

            assertThat(result).containsExactly(jazz);
            verify(eventRepository).findByEventDateBetween(expectedStart, expectedEnd);
        }

        @Test
        @DisplayName("returns empty list when no events occur on that date (edge case)")
        void noEventsOnDate() {
            LocalDate date = LocalDate.of(2000, 1, 1);
            when(eventRepository.findByEventDateBetween(any(), any())).thenReturn(List.of());
            assertThat(eventService.searchByDate(date)).isEmpty();
        }
    }
}
