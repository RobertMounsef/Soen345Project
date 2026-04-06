package com.ticket.system;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * System-level tests: full Spring Boot app, real HTTP, session cookies, and Firebase persistence.
 * <p>
 * Skipped automatically when {@code firebase-service-account.json} is not on the classpath
 * (typical CI clones without secrets). Locally, place the key under {@code src/main/resources/}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TicketApiSystemIT {

    private static final Duration TIMEOUT = Duration.ofSeconds(90);

    private static final Pattern QUOTED_FIELD = Pattern.compile("\"([a-zA-Z]+)\"\\s*:\\s*\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"");

    @LocalServerPort
    private int port;

    private String baseUrl;

    @BeforeAll
    static void requireFirebaseCredentials() {
        boolean present;
        try (InputStream in = TicketApiSystemIT.class.getResourceAsStream("/firebase-service-account.json")) {
            present = in != null;
        } catch (IOException e) {
            present = false;
        }
        Assumptions.assumeTrue(
                present,
                "Skipping API system tests: add firebase-service-account.json under backend/src/main/resources/ (see README).");
    }

    @BeforeEach
    void setBaseUrl() {
        baseUrl = "http://127.0.0.1:" + port;
    }

    @Test
    void sessionEndpoint_unauthenticated_returns401() throws Exception {
        HttpClient client = newHttpClient();
        HttpResponse<String> res = get(client, "/api/auth/session");
        assertEquals(401, res.statusCode(), res.body());
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        HttpClient client = newHttpClient();
        String body = """
                {"identifier":"no-such-user@example.com","password":"wrong"}""";
        HttpResponse<String> res = post(client, "/api/auth/login", body);
        assertEquals(401, res.statusCode(), res.body());
    }

    @Test
    void fullStack_registerOrganizerCreateEvent_registerCustomerReserveListCancel() throws Exception {
        int n = ThreadLocalRandom.current().nextInt(1, 100_001);
        String password = "SysTestPass1!";
        String orgName = "system_test_organizer_" + n;
        String orgEmail = orgName + "@test.com";
        String custName = "system_test_customer_" + n;
        String custEmail = custName + "@test.com";

        HttpClient organizerClient = newHttpClient();
        HttpClient customerClient = newHttpClient();

        assertEquals(200, post(organizerClient, "/api/users", userJson(orgName, orgEmail, null, password, "ORGANIZER")).statusCode());
        assertEquals(200, post(customerClient, "/api/users", userJson(custName, custEmail, null, password, "CUSTOMER")).statusCode());

        assertEquals(200, post(organizerClient, "/api/auth/login", loginJson(orgEmail, password)).statusCode());
        String eventTitle = "system_test_event_" + n;
        String eventJson = """
                {
                  "title": "%s",
                  "category": "SystemTest",
                  "eventDate": "2026-08-20T19:00:00",
                  "location": "Integration Hall",
                  "totalSpots": 4,
                  "status": "ACTIVE"
                }""".formatted(eventTitle);
        HttpResponse<String> createEventRes = post(organizerClient, "/api/events", eventJson);
        assertEquals(200, createEventRes.statusCode(), createEventRes.body());
        String eventId = requireJsonStringField(createEventRes.body(), "eventId");

        assertEquals(200, post(customerClient, "/api/auth/login", loginJson(custEmail, password)).statusCode());

        String reservationJson = "{\"eventId\":\"" + eventId + "\"}";
        HttpResponse<String> reserveRes = post(customerClient, "/api/reservations", reservationJson);
        assertEquals(200, reserveRes.statusCode(), reserveRes.body());
        String reservationId = requireJsonStringField(reserveRes.body(), "reservationId");

        HttpResponse<String> listRes = get(customerClient, "/api/reservations");
        assertEquals(200, listRes.statusCode(), listRes.body());
        assertTrue(listRes.body().contains(reservationId));

        HttpResponse<String> deleteRes = delete(customerClient, "/api/reservations/" + reservationId);
        assertEquals(204, deleteRes.statusCode(), deleteRes.body());

        HttpResponse<String> publicEvents = get(newHttpClient(), "/api/events");
        assertEquals(200, publicEvents.statusCode(), publicEvents.body());
        assertTrue(publicEvents.body().contains(eventTitle));
    }

    static String requireJsonStringField(String json, String fieldName) {
        Matcher m = QUOTED_FIELD.matcher(json);
        while (m.find()) {
            if (fieldName.equals(m.group(1))) {
                return m.group(2);
            }
        }
        throw new AssertionError("Field \"" + fieldName + "\" not found in JSON: " + json);
    }

    private static String userJson(String name, String email, String phone, String password, String role) {
        String phonePart = phone == null ? "null" : "\"" + phone + "\"";
        return """
                {"name":"%s","email":"%s","phone":%s,"password":"%s","role":"%s"}"""
                .formatted(name, email, phonePart, password, role);
    }

    private static String loginJson(String identifier, String password) {
        return """
                {"identifier":"%s","password":"%s"}""".formatted(identifier, password);
    }

    private static HttpClient newHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .build();
    }

    private HttpResponse<String> get(HttpClient client, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(TIMEOUT)
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(HttpClient client, String path, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(HttpClient client, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(TIMEOUT)
                .DELETE()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
