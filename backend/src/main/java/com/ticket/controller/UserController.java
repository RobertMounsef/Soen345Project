package com.ticket.controller;

import com.ticket.model.User;
import com.ticket.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /api/users – Public (registration)
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            return ResponseEntity.ok(userService.createUser(user));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if (msg != null && (msg.contains("required") || msg.contains("Required"))) {
                return ResponseEntity.badRequest().body(Map.of("error", msg));
            }
            return ResponseEntity.status(409).body(Map.of("error", msg));
        }
    }
}