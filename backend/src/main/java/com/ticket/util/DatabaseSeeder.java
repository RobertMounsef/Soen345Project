package com.ticket.util;

import com.ticket.model.Event;
import com.ticket.model.User;
import com.ticket.repository.EventRepository;
import com.ticket.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public DatabaseSeeder(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!eventRepository.findAll().isEmpty()) {
            return;
        }
        System.out.println("Seeding mock data...");

        // Ensure we have an organizer
        User organizer = userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ORGANIZER)
                .findFirst()
                .orElseGet(() -> {
                    User u = new User();
                    u.setName("Adam Organizer");
                    u.setEmail("organizer@test.com");
                    u.setPassword("password");
                    u.setRole(User.Role.ORGANIZER);
                    return userRepository.save(u);
                });

        String orgId = organizer.getUserId();

        createEvent(orgId, "Rock Concert 2026", "Music", 50, "Bell Centre, Montreal");
        createEvent(orgId, "Tech Summit", "Conference", 100, "Convention Centre, Montreal");
        createEvent(orgId, "Jazz Night", "Music", 20, "Modavie, Montreal");
        createEvent(orgId, "Gala Dinner", "Formal", 10, "Ritz-Carlton, Montreal");
        createEvent(orgId, "Local Football Match", "Sports", 200, "Percival Molson Stadium");
        createEvent(orgId, "Stand-up Comedy", "Comedy", 40, "The Comedy Nest");

        System.out.println("Seeding complete!");
    }

    private void createEvent(String orgId, String title, String category, int spots, String location) {
        Event e = new Event();
        e.setOrganizerId(orgId);
        e.setTitle(title);
        e.setCategory(category);
        e.setTotalSpots(spots);
        e.setAvailableSpots(spots);
        e.setLocation(location);
        e.setEventDate(LocalDateTime.now().plusDays((long) (Math.random() * 30) + 1));
        e.setStatus(Event.Status.ACTIVE);
        eventRepository.save(e);
    }
}
