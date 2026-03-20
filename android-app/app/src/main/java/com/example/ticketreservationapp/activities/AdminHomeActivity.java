package com.example.ticketreservationapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.api.ApiService;
import com.example.ticketreservationapp.api.RetrofitClient;
import com.example.ticketreservationapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminHomeActivity extends BaseActivity {

    private TextView tvAdminWelcome;
    private Button btnManageEvents, btnAddEvent, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        tvAdminWelcome = findViewById(R.id.tvAdminWelcome);
        btnManageEvents = findViewById(R.id.btnManageEvents);
        btnAddEvent = findViewById(R.id.btnAddEvent);
        btnLogout = findViewById(R.id.btnAdminLogout);

        tvAdminWelcome.setText("Organizer Panel");

        btnManageEvents.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, ManagedEventsActivity.class);
            startActivity(intent);
        });

        btnAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, AddEditEventActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logoutUser());
    }

    protected void logoutUser() {
        btnLogout.setEnabled(false);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        apiService.logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnLogout.setEnabled(true);
                SessionManager sessionManager = new SessionManager(AdminHomeActivity.this);
                sessionManager.clearSession();
                Intent intent = new Intent(AdminHomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                Toast.makeText(AdminHomeActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnLogout.setEnabled(true);
                Toast.makeText(AdminHomeActivity.this,
                        "Logout failed: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}