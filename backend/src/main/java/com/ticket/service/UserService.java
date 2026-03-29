package com.ticket.service;

import com.ticket.model.User;
import com.ticket.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        boolean hasEmail = user.getEmail() != null && !user.getEmail().isBlank();
        boolean hasPhone = user.getPhone() != null && !user.getPhone().isBlank();
        if (!hasEmail && !hasPhone) {
            throw new IllegalArgumentException("Email or phone number is required");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (userRepository.findByName(user.getName()).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (hasEmail && userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (hasPhone && userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Phone number already registered");
        }
        if (hasEmail) {
            user.setEmail(user.getEmail().trim().toLowerCase(Locale.ROOT));
        }
        if (hasPhone && user.getPhone() != null) {
            user.setPhone(user.getPhone().trim());
        }
        return userRepository.save(user);
    }

    public Optional<User> findByEmailAndPassword(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getPassword().equals(password));
    }

    public Optional<User> findByPhoneAndPassword(String phone, String password) {
        return userRepository.findByPhone(phone)
                .filter(user -> user.getPassword().equals(password));
    }
}