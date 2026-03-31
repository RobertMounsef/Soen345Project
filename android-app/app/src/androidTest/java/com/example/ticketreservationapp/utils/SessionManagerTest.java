package com.example.ticketreservationapp.utils;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class SessionManagerTest {

    private SessionManager sessionManager;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        sessionManager = new SessionManager(context);
        sessionManager.clearSession(); // Ensure fresh state
    }

    @Test
    public void testSaveAndRetrieveSession() {
        String testUserId = "user-12345";
        String testRole = "administrator";

        sessionManager.saveSession(testUserId, testRole);

        assertEquals("Session Manager should return the saved user ID", 
                testUserId, sessionManager.getUserId());
        assertEquals("Session Manager should return the saved role", 
                testRole, sessionManager.getRole());
    }

    @Test
    public void testClearSession() {
        // Save first
        sessionManager.saveSession("user-67890", "customer");
        
        // Then clear
        sessionManager.clearSession();

        assertNull("User ID should be null after clearing session", 
                sessionManager.getUserId());
        assertEquals("Role should be empty after clearing session", 
                "", sessionManager.getRole());
    }

    @Test
    public void testGetEmptyRoleBeforeSave() {
        assertEquals("Role should default to empty string if no session data", 
                "", sessionManager.getRole());
    }
}
