package com.ticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ticket.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // Optional: add custom queries here if needed
}