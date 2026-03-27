package com.ticket.unit;

import com.ticket.model.User;
import com.ticket.repository.UserRepository;
import com.ticket.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User alice;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setUserId("-user001");
        alice.setName("Alice");
        alice.setEmail("alice@test.com");
        alice.setPhone("5140001111");
        alice.setPassword("pass123");
        alice.setRole(User.Role.CUSTOMER);
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("saves and returns the new user")
        void savesUser() {
            when(userRepository.save(alice)).thenReturn(alice);

            User result = userService.createUser(alice);

            assertThat(result).isEqualTo(alice);
            verify(userRepository).save(alice);
        }
    }

    @Nested
    @DisplayName("findByEmailAndPassword")
    class FindByEmailAndPassword {

        @Test
        @DisplayName("returns user when email and password match")
        void correctCredentials() {
            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(alice));

            assertThat(userService.findByEmailAndPassword("alice@test.com", "pass123")).contains(alice);
        }

        @Test
        @DisplayName("returns empty when password does not match")
        void wrongPassword() {
            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(alice));

            assertThat(userService.findByEmailAndPassword("alice@test.com", "WRONG")).isEmpty();
        }

        @Test
        @DisplayName("returns empty when email is not found")
        void emailNotFound() {
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            assertThat(userService.findByEmailAndPassword("unknown@test.com", "pass123")).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByPhoneAndPassword")
    class FindByPhoneAndPassword {

        @Test
        @DisplayName("returns user when phone and password match")
        void correctCredentials() {
            when(userRepository.findByPhone("5140001111")).thenReturn(Optional.of(alice));

            assertThat(userService.findByPhoneAndPassword("5140001111", "pass123")).contains(alice);
        }

        @Test
        @DisplayName("returns empty when password is wrong")
        void wrongPassword() {
            when(userRepository.findByPhone("5140001111")).thenReturn(Optional.of(alice));

            assertThat(userService.findByPhoneAndPassword("5140001111", "WRONG")).isEmpty();
        }

        @Test
        @DisplayName("returns empty when phone is not registered")
        void phoneNotFound() {
            when(userRepository.findByPhone("0000000000")).thenReturn(Optional.empty());

            assertThat(userService.findByPhoneAndPassword("0000000000", "pass123")).isEmpty();
        }
    }
}