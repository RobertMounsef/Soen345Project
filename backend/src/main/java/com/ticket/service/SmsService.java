package com.ticket.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    private final boolean active;
    private final String fromNumber;

    public SmsService(
            @Value("${app.sms.enabled:false}") boolean enabled,
            @Value("${twilio.account.sid:}") String accountSid,
            @Value("${twilio.auth.token:}") String authToken,
            @Value("${twilio.from.number:}") String fromNumber) {
        this.fromNumber = fromNumber != null ? fromNumber.trim() : "";
        boolean creds = accountSid != null && !accountSid.isBlank()
                && authToken != null && !authToken.isBlank()
                && !this.fromNumber.isBlank();
        this.active = enabled && creds;
        if (this.active) {
            Twilio.init(accountSid, authToken);
        } else if (enabled && !creds) {
            log.warn("SMS is enabled but Twilio credentials or from-number are missing; SMS will not be sent.");
        }
    }

    @Async
    public void sendReservationConfirmation(String toE164, String userName, String eventTitle) {
        if (!active) {
            log.debug("SMS disabled or not configured; skipping reservation confirmation SMS.");
            return;
        }
        if (toE164 == null || toE164.isBlank()) {
            return;
        }
        String body = "Hi " + userName + ", your reservation for \"" + eventTitle + "\" is confirmed. "
                + "You can cancel anytime in the app.";
        send(toE164.trim(), body);
    }

    @Async
    public void sendReservationCancellation(String toE164, String userName, String eventTitle) {
        if (!active) {
            log.debug("SMS disabled or not configured; skipping reservation cancellation SMS.");
            return;
        }
        if (toE164 == null || toE164.isBlank()) {
            return;
        }
        String body = "Hi " + userName + ", your reservation for \"" + eventTitle + "\" has been cancelled.";
        send(toE164.trim(), body);
    }

    private void send(String toE164, String body) {
        try {
            Message.creator(new PhoneNumber(toE164), new PhoneNumber(fromNumber), body).create();
        } catch (Exception e) {
            log.warn("Failed to send SMS to {}: {}", toE164, e.toString());
        }
    }
}
