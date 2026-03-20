package com.example.ticketreservationapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketreservationapp.api.ApiService;
import com.example.ticketreservationapp.api.RetrofitClient;
import com.example.ticketreservationapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();
        
        if (!(this instanceof HomeActivity) && !(this instanceof AdminHomeActivity) && 
            !(this instanceof LoginActivity) && !(this instanceof RegisterActivity)) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        android.view.View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (this instanceof LoginActivity || this instanceof RegisterActivity) return false;
        
        SessionManager sessionManager = new SessionManager(this);
        String role = sessionManager.getRole();
        
        MenuItem itemHome = menu.add(Menu.NONE, 1, Menu.NONE, "Home");
        itemHome.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        
        if ("USER".equalsIgnoreCase(role) || "CUSTOMER".equalsIgnoreCase(role)) {
            MenuItem itemBrowse = menu.add(Menu.NONE, 2, Menu.NONE, "Browse Events");
            itemBrowse.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            
            MenuItem itemMyRes = menu.add(Menu.NONE, 3, Menu.NONE, "My Reservations");
            itemMyRes.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        } else if ("ORGANIZER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
            MenuItem itemAddEvent = menu.add(Menu.NONE, 6, Menu.NONE, "Add New Event");
            itemAddEvent.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            
            MenuItem itemManage = menu.add(Menu.NONE, 4, Menu.NONE, "Manage Events");
            itemManage.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        
        MenuItem itemLogout = menu.add(Menu.NONE, 5, Menu.NONE, "Logout");
        itemLogout.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        
        switch (item.getItemId()) {
            case 1:
                SessionManager sessionManager = new SessionManager(this);
                Intent homeIntent;
                if ("ORGANIZER".equalsIgnoreCase(sessionManager.getRole()) || "ADMIN".equalsIgnoreCase(sessionManager.getRole())) {
                    homeIntent = new Intent(this, AdminHomeActivity.class);
                } else {
                    homeIntent = new Intent(this, HomeActivity.class);
                }
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(homeIntent);
                return true;
            case 2:
                Intent eventListIntent = new Intent(this, EventListActivity.class);
                startActivity(eventListIntent);
                return true;
            case 3:
                Intent myResIntent = new Intent(this, MyReservationsActivity.class);
                startActivity(myResIntent);
                return true;
            case 4:
                Intent manageIntent = new Intent(this, ManagedEventsActivity.class);
                startActivity(manageIntent);
                return true;
            case 6:
                Intent addEventIntent = new Intent(this, AddEditEventActivity.class);
                addEventIntent.putExtra("isEdit", false);
                startActivity(addEventIntent);
                return true;
            case 5:
                logoutUser();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    protected void logoutUser() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                performLogout();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                performLogout();
            }
        });
    }
    
    private void performLogout() {
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
    }
}
