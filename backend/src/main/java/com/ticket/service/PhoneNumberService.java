package com.ticket.service;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PhoneNumberService {

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
    private final String defaultRegion;

    public PhoneNumberService(@Value("${app.phone.default-region:CA}") String defaultRegion) {
        this.defaultRegion = defaultRegion;
    }

    /**
     * Parses and validates a phone number, returning E.164 (e.g. {@code +15145550199}).
     */
    public String normalizeToE164(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Phone number is required when provided");
        }
        String trimmed = raw.trim();
        try {
            Phonenumber.PhoneNumber parsed = phoneUtil.parse(trimmed, defaultRegion);
            if (!phoneUtil.isValidNumber(parsed)) {
                throw new IllegalArgumentException(
                        "Invalid phone number. Use international format, e.g. +15145550199.");
            }
            return phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw new IllegalArgumentException(
                    "Invalid phone number. Use international format, e.g. +15145550199.", e);
        }
    }

    /**
     * Same as {@link #normalizeToE164(String)} but returns empty when input is blank
     * or cannot be parsed as a valid number.
     */
    public Optional<String> tryNormalizeToE164(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            Phonenumber.PhoneNumber parsed = phoneUtil.parse(raw.trim(), defaultRegion);
            if (!phoneUtil.isValidNumber(parsed)) {
                return Optional.empty();
            }
            return Optional.of(phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164));
        } catch (NumberParseException e) {
            return Optional.empty();
        }
    }
}
