package com.example.ticketreservationapp.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    private static final String[] INPUT_PATTERNS = {
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
    };

    private static final String OUTPUT_PATTERN = "MMM d, yyyy • h:mm a";

    public static String formatDateTime(String rawDateTime) {
        if (rawDateTime == null || rawDateTime.trim().isEmpty()) {
            return "N/A";
        }

        for (String pattern : INPUT_PATTERNS) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(pattern, Locale.US);
                inputFormat.setLenient(false);

                Date date = inputFormat.parse(rawDateTime);
                if (date != null) {
                    SimpleDateFormat outputFormat = new SimpleDateFormat(OUTPUT_PATTERN, Locale.US);
                    return outputFormat.format(date);
                }
            } catch (ParseException ignored) {
            }
        }

        return rawDateTime;
    }
}