package com.ticket.integration;

import com.ticket.model.User;
import com.ticket.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User alice;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setName("Alice");
        alice.setEmail("alice@test.com");
        alice.setPhone("5141234567");
        alice.setPassword("secret");
        alice.setRole(User.Role.CUSTOMER);
        userRepository.save(alice);
    }

    // findByEmail

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("returns user when email exists")
        void found() {
            Optional<User> result = userRepository.findByEmail("alice@test.com");
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("returns empty when email does not exist (edge case)")
        void notFound() {
            Optional<User> result = userRepository.findByEmail("nobody@test.com");
            assertThat(result).isEmpty();
        }
    }

    //findByPhone

    @Nested
    @DisplayName("findByPhone")
    class FindByPhone {

        @Test
        @DisplayName("returns user when phone exists")
        void found() {
            Optional<User> result = userRepository.findByPhone("5141234567");
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("returns empty when phone does not exist (edge case)")
        void notFound() {
            Optional<User> result = userRepository.findByPhone("0000000000");
            assertThat(result).isEmpty();
        }
    }

    //save

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("persists user and generates ID")
        void savesUser() {
            User bob = new User();
            bob.setName("Bob");
            bob.setEmail("bob@test.com");
            bob.setPassword("pass");
            bob.setRole(User.Role.ORGANIZER);

            User saved = userRepository.save(bob);

            assertThat(saved.getUserId()).isNotNull();
            assertThat(userRepository.findByEmail("bob@test.com")).isPresent();
        }
    }
}