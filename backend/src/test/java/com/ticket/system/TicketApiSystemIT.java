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

        HttpResponse<String> orgSession = get(organizerClient, "/api/auth/session");
        assertEquals(200, orgSession.statusCode(), orgSession.body());
        assertTrue(orgSession.body().contains("ORGANIZER"), orgSession.body());

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

        HttpResponse<String> publicEventById = get(newHttpClient(), "/api/events/" + eventId);
        assertEquals(200, publicEventById.statusCode(), publicEventById.body());
        assertTrue(publicEventById.body().contains(eventTitle), publicEventById.body());

        HttpResponse<String> filteredEvents = get(newHttpClient(), "/api/events?category=SystemTest");
        assertEquals(200, filteredEvents.statusCode(), filteredEvents.body());
        assertTrue(filteredEvents.body().contains(eventTitle), filteredEvents.body());

        assertEquals(200, post(customerClient, "/api/auth/login", loginJson(custEmail, password)).statusCode());
        HttpResponse<String> custSession = get(customerClient, "/api/auth/session");
        assertEquals(200, custSession.statusCode(), custSession.body());
        assertTrue(custSession.body().contains("CUSTOMER"), custSession.body());

        assertEquals(403, post(customerClient, "/api/events", eventJson).statusCode());

        String reservationJson = "{\"eventId\":\"" + eventId + "\"}";
        assertEquals(403, post(organizerClient, "/api/reservations", reservationJson).statusCode());

        HttpResponse<String> reserveRes = post(customerClient, "/api/reservations", reservationJson);
        assertEquals(200, reserveRes.statusCode(), reserveRes.body());
        String reservationId = requireJsonStringField(reserveRes.body(), "reservationId");

        HttpResponse<String> dupReserve = post(customerClient, "/api/reservations", reservationJson);
        assertEquals(409, dupReserve.statusCode(), dupReserve.body());

        HttpResponse<String> orgEventReservations = get(organizerClient, "/api/reservations/event/" + eventId);
        assertEquals(200, orgEventReservations.statusCode(), orgEventReservations.body());
        assertTrue(orgEventReservations.body().contains(reservationId), orgEventReservations.body());

        HttpResponse<String> custReservationById = get(customerClient, "/api/reservations/" + reservationId);
        assertEquals(200, custReservationById.statusCode(), custReservationById.body());
        assertTrue(custReservationById.body().contains(eventId), custReservationById.body());

        HttpResponse<String> listRes = get(customerClient, "/api/reservations");
        assertEquals(200, listRes.statusCode(), listRes.body());
        assertTrue(listRes.body().contains(reservationId));

        HttpResponse<String> deleteRes = delete(customerClient, "/api/reservations/" + reservationId);
        assertEquals(204, deleteRes.statusCode(), deleteRes.body());

        String updatedTitle = eventTitle + "_updated";
        String updateEventJson = """
                {
                  "title": "%s",
                  "category": "SystemTest",
                  "eventDate": "2026-08-21T20:00:00",
                  "location": "Updated Integration Hall",
                  "totalSpots": 4,
                  "availableSpots": 4,
                  "status": "ACTIVE"
                }""".formatted(updatedTitle);
        HttpResponse<String> putRes = put(organizerClient, "/api/events/" + eventId, updateEventJson);
        assertEquals(200, putRes.statusCode(), putRes.body());

        HttpResponse<String> eventAfterPut = get(newHttpClient(), "/api/events/" + eventId);
        assertEquals(200, eventAfterPut.statusCode(), eventAfterPut.body());
        assertTrue(eventAfterPut.body().contains(updatedTitle), eventAfterPut.body());

        HttpResponse<String> publicEvents = get(newHttpClient(), "/api/events");
        assertEquals(200, publicEvents.statusCode(), publicEvents.body());
        assertTrue(publicEvents.body().contains(updatedTitle), publicEvents.body());

        assertEquals(204, delete(organizerClient, "/api/events/" + eventId).statusCode());
        assertEquals(404, get(newHttpClient(), "/api/events/" + eventId).statusCode());
    }

    @Test
    void logout_invalidatesSession() throws Exception {
        int n = ThreadLocalRandom.current().nextInt(1, 100_001);
        String password = "SysTestPass1!";
        String name = "system_test_logout_" + n;
        String email = name + "@test.com";
        HttpClient client = newHttpClient();
        assertEquals(200, post(client, "/api/users", userJson(name, email, null, password, "CUSTOMER")).statusCode());
        assertEquals(200, post(client, "/api/auth/login", loginJson(email, password)).statusCode());
        assertEquals(200, get(client, "/api/auth/session").statusCode());
        assertEquals(200, postEmpty(client, "/api/auth/logout").statusCode());
        assertEquals(401, get(client, "/api/auth/session").statusCode());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        int n = ThreadLocalRandom.current().nextInt(1, 100_001);
        String password = "SysTestPass1!";
        String name = "system_test_dup_" + n;
        String email = name + "@test.com";
        HttpClient client = newHttpClient();
        assertEquals(200, post(client, "/api/users", userJson(name, email, null, password, "CUSTOMER")).statusCode());
        String otherName = "system_test_dup_other_" + n;
        HttpResponse<String> second = post(client, "/api/users", userJson(otherName, email, null, password, "CUSTOMER"));
        assertEquals(409, second.statusCode(), second.body());
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

    private HttpResponse<String> put(HttpClient client, String path, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
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

    private HttpResponse<String> postEmpty(HttpClient client, String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(TIMEOUT)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
