package com.ticket.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    private final boolean active;
    private final String fromNumber;
    private final String messagingServiceSid;

    public SmsService(
            @Value("${app.sms.enabled:false}") boolean enabled,
            @Value("${twilio.account.sid:}") String accountSid,
            @Value("${twilio.auth.token:}") String authToken,
            @Value("${twilio.from.number:}") String fromNumber,
            @Value("${twilio.messaging.service.sid:}") String messagingServiceSid) {
        this.fromNumber = fromNumber != null ? fromNumber.trim() : "";
        this.messagingServiceSid = messagingServiceSid != null ? messagingServiceSid.trim() : "";

        String sid = accountSid != null ? accountSid.trim() : "";
        boolean tokenOk = authToken != null && !authToken.isBlank();
        boolean hasMessagingService = StringUtils.hasText(this.messagingServiceSid);
        boolean hasFrom = StringUtils.hasText(this.fromNumber);

        // Twilio REST auth always uses Account SID (AC...), never Messaging Service (MG...).
        boolean authSidOk = sid.startsWith("AC");
        if (enabled && !sid.isEmpty() && sid.startsWith("MG")) {
            log.error(
                    "twilio.account.sid is set to a Messaging Service SID (MG...). "
                            + "Put that value in twilio.messaging.service.sid instead, and set twilio.account.sid "
                            + "to your Account SID from Twilio Console (starts with AC). SMS is disabled until fixed.");
        }

        boolean canSend = hasMessagingService || hasFrom;
        this.active = enabled && authSidOk && tokenOk && canSend;

        if (this.active) {
            Twilio.init(sid, authToken.trim());
            log.info(
                    "Twilio SMS enabled (sending via {}).",
                    hasMessagingService ? "Messaging Service " + this.messagingServiceSid : "phone number " + this.fromNumber);
        } else if (enabled) {
            if (!authSidOk) {
                log.warn(
                        "SMS is enabled but twilio.account.sid must be your Twilio Account SID (starts with AC). "
                                + "SMS will not be sent.");
            } else if (!tokenOk) {
                log.warn("SMS is enabled but twilio.auth.token is missing. SMS will not be sent.");
            } else if (!canSend) {
                log.warn(
                        "SMS is enabled but neither twilio.messaging.service.sid nor twilio.from.number is set. "
                                + "SMS will not be sent.");
            }
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
            if (StringUtils.hasText(messagingServiceSid)) {
                Message.creator(new PhoneNumber(toE164), messagingServiceSid, body).create();
            } else {
                Message.creator(new PhoneNumber(toE164), new PhoneNumber(fromNumber), body).create();
            }
        } catch (Exception e) {
            log.warn("Failed to send SMS to {}: {}", toE164, e.toString());
        }
    }
}
