package com.ticket.service;

import com.ticket.model.User;
import com.ticket.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(Integer id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setName(updatedUser.getName());
            user.setEmail(updatedUser.getEmail());
            user.setPhone(updatedUser.getPhone());
            user.setPassword(updatedUser.getPassword());
            user.setRole(updatedUser.getRole());
            user.setLastUpdate(LocalDateTime.now());
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }

    /**
     * Looks up a user by email and validates the password.
     * NOTE: Passwords are compared as plain text here.
     * Consider using BCrypt hashing in production.
     */
    public Optional<User> findByEmailAndPassword(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getPassword().equals(password));
    }

    /**
     * Looks up a user by phone number and validates the password.
     */
    public Optional<User> findByPhoneAndPassword(String phone, String password) {
        return userRepository.findByPhone(phone)
                .filter(user -> user.getPassword().equals(password));
    }
}