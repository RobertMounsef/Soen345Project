package com.example.ticketreservationapp.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class DateTimeUtilsTest {

    @Test
    public void testFormatDateTime_withMicroseconds() {
        String input = "2026-04-15T14:30:00.123456";
        String result = DateTimeUtils.formatDateTime(input);
        assertEquals("Apr 15, 2026 • 2:30 PM", result);
    }

    @Test
    public void testFormatDateTime_withMilliseconds() {
        String input = "2026-05-20T09:15:00.123";
        String result = DateTimeUtils.formatDateTime(input);
        assertEquals("May 20, 2026 • 9:15 AM", result);
    }

    @Test
    public void testFormatDateTime_withoutMilliseconds() {
        String input = "2026-12-25T18:00:00";
        String result = DateTimeUtils.formatDateTime(input);
        assertEquals("Dec 25, 2026 • 6:00 PM", result);
    }

    @Test
    public void testFormatDateTime_withSpaceInsteadOfT() {
        String input = "2026-01-01 10:00:00";
        String result = DateTimeUtils.formatDateTime(input);
        assertEquals("Jan 1, 2026 • 10:00 AM", result);
    }

    @Test
    public void testFormatDateTime_nullInput() {
        String result = DateTimeUtils.formatDateTime(null);
        assertEquals("N/A", result);
    }

    @Test
    public void testFormatDateTime_emptyInput() {
        String result = DateTimeUtils.formatDateTime("   ");
        assertEquals("N/A", result);
    }

    @Test
    public void testFormatDateTime_invalidInputReturnsOriginal() {
        String input = "invalid-date-format";
        String result = DateTimeUtils.formatDateTime(input);
        assertEquals("invalid-date-format", result);
    }
}
