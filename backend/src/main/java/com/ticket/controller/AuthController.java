package com.ticket.controller;

import com.ticket.model.User;
import com.ticket.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Session attribute keys – referenced by other controllers to read session data
    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USER_ROLE = "userRole";

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    
    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody LoginRequest loginRequest,
            HttpSession session) {

        String identifier = loginRequest.getIdentifier();
        String password = loginRequest.getPassword();

        if (identifier == null || identifier.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Identifier and password are required"));
        }

        // Try email first, then phone
        Optional<User> userOpt = userService.findByEmailAndPassword(identifier, password);
        if (userOpt.isEmpty()) {
            userOpt = userService.findByPhoneAndPassword(identifier, password);
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid credentials"));
        }

        User user = userOpt.get();

        // Populate the session
        session.setAttribute(SESSION_USER_ID, user.getUserId());
        session.setAttribute(SESSION_USER_ROLE, user.getRole().name());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("userId", user.getUserId());
        response.put("role", user.getRole().name());

        return ResponseEntity.ok(response);
    }

    
    //GET /api/auth/session
    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> getSession(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(SESSION_USER_ID);
        String role = (String) session.getAttribute(SESSION_USER_ROLE);

        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "No active session"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("role", role);

        return ResponseEntity.ok(response);
    }


    //POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // ----- Inner DTO -----

    public static class LoginRequest {
        /** Email address OR phone number */
        private String identifier;
        private String password;

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
