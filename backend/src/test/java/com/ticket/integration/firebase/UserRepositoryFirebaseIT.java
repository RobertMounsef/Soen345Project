package com.ticket.integration.firebase;

import com.ticket.model.User;
import com.ticket.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserRepository (Firebase integration)")
class UserRepositoryFirebaseIT extends AbstractFirebaseRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("save assigns userId; findById returns stored user")
    void saveAndFindById() {
        User u = new User();
        u.setName("IT User");
        u.setEmail("it-user-" + System.nanoTime() + "@example.com");
        u.setPassword("secret");
        u.setRole(User.Role.CUSTOMER);

        User saved = userRepository.save(u);

        assertThat(saved.getUserId()).isNotBlank();

        Optional<User> found = userRepository.findById(saved.getUserId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("IT User");
        assertThat(found.get().getEmail()).isEqualToIgnoringCase(saved.getEmail());
    }

    @Test
    @DisplayName("findByEmail is case-insensitive")
    void findByEmailCaseInsensitive() {
        String local = "it-mixed-" + System.nanoTime();
        User u = new User();
        u.setName("Mixed");
        u.setEmail(local + "@Example.COM");
        u.setPassword("p");
        u.setRole(User.Role.CUSTOMER);
        userRepository.save(u);

        assertThat(userRepository.findByEmail(local + "@example.com")).isPresent();
        assertThat(userRepository.findByEmail(local + "@EXAMPLE.COM")).isPresent();
    }

    @Test
    @DisplayName("findByPhone returns user when phone matches exactly")
    void findByPhoneExactMatch() {
        String phone = "514IT" + (System.nanoTime() % 1_000_000_000L);
        User u = new User();
        u.setName("Phone User");
        u.setPhone(phone);
        u.setPassword("p");
        u.setRole(User.Role.CUSTOMER);
        userRepository.save(u);

        Optional<User> found = userRepository.findByPhone(phone);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Phone User");
    }

    @Test
    @DisplayName("findByEmail returns empty for unknown address")
    void findByEmailMissing() {
        assertThat(userRepository.findByEmail("missing-" + UUID.randomUUID() + "@example.com")).isEmpty();
    }
}
