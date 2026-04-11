package com.example.ticketreservationapp.utils;

import java.util.regex.Pattern;

/**
 * Client-side helpers so signup/login send E.164-style numbers the API expects.
 */
public final class PhoneFormatUtils {

    private static final Pattern E164 = Pattern.compile("^\\+[1-9]\\d{6,14}$");

    private PhoneFormatUtils() {
    }

    /**
     * Normalizes common North American input to E.164 (+country + national digits).
     * If the value already starts with {@code +}, non-digits after the plus are stripped.
     */
    public static String normalizeForApi(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return "";
        }
        if (s.startsWith("+")) {
            return "+" + s.substring(1).replaceAll("\\D", "");
        }
        String digits = s.replaceAll("\\D", "");
        if (digits.length() == 10) {
            return "+1" + digits;
        }
        if (digits.length() == 11 && digits.startsWith("1")) {
            return "+" + digits;
        }
        return s;
    }

    public static boolean isValidE164(String normalized) {
        return normalized != null && E164.matcher(normalized).matches();
    }
}
