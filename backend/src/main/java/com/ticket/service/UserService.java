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