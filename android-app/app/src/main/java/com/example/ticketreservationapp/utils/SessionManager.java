package com.example.ticketreservationapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "ticket_app_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(Integer userId, String role) {
        prefs.edit()
                .putInt(KEY_USER_ID, userId != null ? userId : -1)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "");
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}