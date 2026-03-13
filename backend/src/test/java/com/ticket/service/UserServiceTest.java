package com.ticket.service;

import com.ticket.model.User;
import com.ticket.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // ── Shared test fixture ─────────────────────────────────────────
    private User makeUser(Integer id, String name, String email, String phone, String password, User.Role role) {
        User u = new User();
        u.setUserId(id);
        u.setName(name);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPassword(password);
        u.setRole(role);
        return u;
    }

    private User alice;

    @BeforeEach
    void setUp() {
        alice = makeUser(1, "Alice", "alice@test.com", "5140001111", "pass123", User.Role.CUSTOMER);
    }

    // ── getAllUsers ────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("returns every user from the repository")
        void returnsAllUsers() {
            User bob = makeUser(2, "Bob", "bob@test.com", "5140002222", "pass456", User.Role.ADMIN);
            when(userRepository.findAll()).thenReturn(List.of(alice, bob));

            List<User> result = userService.getAllUsers();

            assertThat(result).hasSize(2).contains(alice, bob);
        }

        @Test
        @DisplayName("returns empty list when no users exist (edge case)")
        void returnsEmptyList() {
            when(userRepository.findAll()).thenReturn(List.of());

            assertThat(userService.getAllUsers()).isEmpty();
        }
    }

    // ── getUserById ────────────────────────────────────────────────

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("returns user when found")
        void found() {
            when(userRepository.findById(1)).thenReturn(Optional.of(alice));

            assertThat(userService.getUserById(1)).contains(alice);
        }

        @Test
        @DisplayName("returns empty when user does not exist (edge case)")
        void notFound() {
            when(userRepository.findById(99)).thenReturn(Optional.empty());

            assertThat(userService.getUserById(99)).isEmpty();
        }
    }

    // ── createUser ────────────────────────────────────────────────

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

    // ── updateUser ────────────────────────────────────────────────

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("updates all fields and saves")
        void updatesFields() {
            User updated = makeUser(null, "Alice Updated", "new@test.com", "5140009999", "newpass", User.Role.ADMIN);
            when(userRepository.findById(1)).thenReturn(Optional.of(alice));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.updateUser(1, updated);

            assertThat(result.getName()).isEqualTo("Alice Updated");
            assertThat(result.getEmail()).isEqualTo("new@test.com");
            assertThat(result.getRole()).isEqualTo(User.Role.ADMIN);
        }

        @Test
        @DisplayName("throws RuntimeException when user does not exist (edge case)")
        void userNotFound() {
            when(userRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(99, alice))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
        }
    }

    // ── deleteUser ────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("delegates to repository deleteById")
        void deletesUser() {
            userService.deleteUser(1);
            verify(userRepository).deleteById(1);
        }
    }

    // ── findByEmailAndPassword ────────────────────────────────────

    @Nested
    @DisplayName("findByEmailAndPassword")
    class FindByEmailAndPassword {

        @Test
        @DisplayName("returns user when email and password match")
        void correctCredentials() {
            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(alice));

            Optional<User> result = userService.findByEmailAndPassword("alice@test.com", "pass123");

            assertThat(result).contains(alice);
        }

        @Test
        @DisplayName("returns empty when password does not match (edge case)")
        void wrongPassword() {
            when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(alice));

            assertThat(userService.findByEmailAndPassword("alice@test.com", "WRONG")).isEmpty();
        }

        @Test
        @DisplayName("returns empty when email is not found (edge case)")
        void emailNotFound() {
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            assertThat(userService.findByEmailAndPassword("unknown@test.com", "pass123")).isEmpty();
        }
    }

    // ── findByPhoneAndPassword ────────────────────────────────────

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
        @DisplayName("returns empty when phone exists but password is wrong (edge case)")
        void wrongPassword() {
            when(userRepository.findByPhone("5140001111")).thenReturn(Optional.of(alice));

            assertThat(userService.findByPhoneAndPassword("5140001111", "WRONG")).isEmpty();
        }

        @Test
        @DisplayName("returns empty when phone number is not registered (edge case)")
        void phoneNotFound() {
            when(userRepository.findByPhone("0000000000")).thenReturn(Optional.empty());

            assertThat(userService.findByPhoneAndPassword("0000000000", "pass123")).isEmpty();
        }
    }
}
