package com.ticket.integration.firebase;

import com.ticket.model.Event;
import com.ticket.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventRepository (Firebase integration)")
class EventRepositoryFirebaseIT extends AbstractFirebaseRepositoryIT {

    @Autowired
    private EventRepository eventRepository;

    private Event newJazzEvent() {
        Event e = new Event();
        e.setTitle("IT Jazz " + System.nanoTime());
        e.setCategory("music");
        e.setLocation("Montreal");
        e.setEventDate(LocalDateTime.of(2026, 6, 15, 20, 0));
        e.setTotalSpots(100);
        e.setAvailableSpots(100);
        e.setStatus(Event.Status.ACTIVE);
        e.setOrganizerId("it-org-1");
        return e;
    }

    private Event newSportsEvent() {
        Event e = new Event();
        e.setTitle("IT Sports " + System.nanoTime());
        e.setCategory("gaming");
        e.setLocation("Toronto");
        e.setEventDate(LocalDateTime.of(2026, 7, 10, 19, 0));
        e.setTotalSpots(50);
        e.setAvailableSpots(50);
        e.setStatus(Event.Status.ACTIVE);
        e.setOrganizerId("it-org-1");
        return e;
    }

    @Test
    @DisplayName("findByCategoryIgnoreCase filters stored events")
    void findByCategory() {
        Event jazz = eventRepository.save(newJazzEvent());
        Event sports = eventRepository.save(newSportsEvent());

        List<Event> music = eventRepository.findByCategoryIgnoreCase("MUSIC");
        assertThat(music).extracting(Event::getEventId).contains(jazz.getEventId());
        assertThat(music).extracting(Event::getEventId).doesNotContain(sports.getEventId());
    }

    @Test
    @DisplayName("findByLocationContainingIgnoreCase matches substring")
    void findByLocation() {
        Event e = eventRepository.save(newJazzEvent());

        List<Event> toronto = eventRepository.findByLocationContainingIgnoreCase("mont");
        assertThat(toronto).extracting(Event::getEventId).contains(e.getEventId());
    }

    @Test
    @DisplayName("findByEventDateBetween includes events on that calendar day")
    void findByEventDateBetween() {
        Event jazz = eventRepository.save(newJazzEvent());
        eventRepository.save(newSportsEvent());

        LocalDateTime start = LocalDateTime.of(2026, 6, 15, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 15, 23, 59, 59);

        List<Event> onDay = eventRepository.findByEventDateBetween(start, end);
        assertThat(onDay).extracting(Event::getEventId).contains(jazz.getEventId());
    }

    @Test
    @DisplayName("save then findById round-trips fields")
    void saveAndFindById() {
        Event in = newJazzEvent();
        Event saved = eventRepository.save(in);

        assertThat(saved.getEventId()).isNotBlank();
        assertThat(eventRepository.findById(saved.getEventId())).hasValueSatisfying(e -> {
            assertThat(e.getTitle()).isEqualTo(in.getTitle());
            assertThat(e.getCategory()).isEqualTo("music");
        });
    }

    @Test
    @DisplayName("deleteById removes event")
    void deleteById() {
        Event saved = eventRepository.save(newJazzEvent());
        String id = saved.getEventId();

        eventRepository.deleteById(id);

        assertThat(eventRepository.findById(id)).isEmpty();
    }
}
