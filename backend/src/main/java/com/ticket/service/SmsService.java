package com.ticket.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${app.sms.twilio-account-sid:}")
    private String twilioAccountSid;

    @Value("${app.sms.twilio-auth-token:}")
    private String twilioAuthToken;

    @Value("${app.sms.twilio-from-number:}")
    private String twilioFromNumber;

    public void sendReservationConfirmation(String toPhone, String userName, String eventTitle) {
        String text = "Hi " + userName + ", your reservation for \"" + eventTitle
                + "\" is confirmed. Thank you.";
        send(toPhone, text);
    }

    public void sendReservationCancellation(String toPhone, String userName, String eventTitle) {
        String text = "Hi " + userName + ", your reservation for \"" + eventTitle + "\" was cancelled.";
        send(toPhone, text);
    }

    private void send(String toPhone, String body) {
        if (toPhone == null || toPhone.isBlank()) {
            return;
        }
        if (twilioAccountSid.isBlank() || twilioAuthToken.isBlank() || twilioFromNumber.isBlank()) {
            log.info("[SMS not configured] To {}: {}", toPhone, body);
            return;
        }
        try {
            String form = "To=" + URLEncoder.encode(toPhone, StandardCharsets.UTF_8)
                    + "&From=" + URLEncoder.encode(twilioFromNumber, StandardCharsets.UTF_8)
                    + "&Body=" + URLEncoder.encode(body, StandardCharsets.UTF_8);
            String url = "https://api.twilio.com/2010-04-01/Accounts/"
                    + twilioAccountSid + "/Messages.json";
            String auth = twilioAccountSid + ":" + twilioAuthToken;
            String basic = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Basic " + basic)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Twilio SMS failed: HTTP {} {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("SMS send failed: {}", e.getMessage());
        }
    }
}
