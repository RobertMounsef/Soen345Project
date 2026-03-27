package com.ticket.integration;

import com.ticket.model.User;
import com.ticket.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepository")
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private User alice;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setUserId("-userAlice001");
        alice.setName("Alice");
        alice.setEmail("alice@test.com");
        alice.setPhone("5141234567");
        alice.setPassword("secret");
        alice.setRole(User.Role.CUSTOMER);
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("returns user when email exists")
        void found() {
            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(alice));

            Optional<User> result = userRepository.findByEmail("alice@test.com");

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("returns empty when email does not exist (edge case)")
        void notFound() {
            when(userRepository.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

            Optional<User> result = userRepository.findByEmail("nobody@test.com");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByPhone")
    class FindByPhone {

        @Test
        @DisplayName("returns user when phone exists")
        void found() {
            when(userRepository.findByPhone("5141234567")).thenReturn(Optional.of(alice));

            Optional<User> result = userRepository.findByPhone("5141234567");

            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("returns empty when phone does not exist (edge case)")
        void notFound() {
            when(userRepository.findByPhone("0000000000")).thenReturn(Optional.empty());

            Optional<User> result = userRepository.findByPhone("0000000000");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("persists user and returns it with an ID")
        void savesUser() {
            User bob = new User();
            bob.setName("Bob");
            bob.setEmail("bob@test.com");
            bob.setPassword("pass");
            bob.setRole(User.Role.ORGANIZER);

            User saved = new User();
            saved.setUserId("-userBob002");
            saved.setName("Bob");
            saved.setEmail("bob@test.com");
            saved.setPassword("pass");
            saved.setRole(User.Role.ORGANIZER);

            when(userRepository.save(bob)).thenReturn(saved);
            when(userRepository.findByEmail("bob@test.com")).thenReturn(Optional.of(saved));

            User result = userRepository.save(bob);

            assertThat(result.getUserId()).isNotNull();
            assertThat(userRepository.findByEmail("bob@test.com")).isPresent();
        }
    }
}