package com.ticket.service;

import com.ticket.model.User;
import com.ticket.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        if (user.getName() != null && !user.getName().isBlank()
                && userRepository.findByName(user.getName()).isPresent()) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()
                && userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (user.getPhone() != null && !user.getPhone().isBlank()
                && userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Phone number already registered");
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