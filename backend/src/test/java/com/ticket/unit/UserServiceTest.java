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

        @Test
        @DisplayName("allows phone-only registration when email is blank")
        void phoneOnly() {
            User phoneUser = new User();
            phoneUser.setName("Bob");
            phoneUser.setEmail("");
            phoneUser.setPhone("5145550199");
            phoneUser.setPassword("secret");
            phoneUser.setRole(User.Role.CUSTOMER);
            when(userRepository.findByName("Bob")).thenReturn(Optional.empty());
            when(userRepository.findByPhone("5145550199")).thenReturn(Optional.empty());
            when(userRepository.save(phoneUser)).thenReturn(phoneUser);

            User result = userService.createUser(phoneUser);

            assertThat(result.getPhone()).isEqualTo("5145550199");
            verify(userRepository).save(phoneUser);
        }

        @Test
        @DisplayName("throws when neither email nor phone is provided")
        void rejectsMissingContact() {
            User bad = new User();
            bad.setName("x");
            bad.setPassword("p");
            bad.setRole(User.Role.CUSTOMER);

            assertThatThrownBy(() -> userService.createUser(bad))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email or phone");
        }

        @Test
        @DisplayName("throws when name is blank")
        void rejectsBlankName() {
            alice.setName("  ");
            assertThatThrownBy(() -> userService.createUser(alice))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Name");
        }

        @Test
        @DisplayName("throws when password is blank")
        void rejectsBlankPassword() {
            alice.setPassword("");
            assertThatThrownBy(() -> userService.createUser(alice))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Password");
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

        @Test
        @DisplayName("repository resolves case-insensitive email (mixed case in lookup)")
        void emailCaseInsensitive() {
            when(userRepository.findByEmail("ALICE@test.com")).thenReturn(Optional.of(alice));

            assertThat(userService.findByEmailAndPassword("ALICE@test.com", "pass123")).contains(alice);
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