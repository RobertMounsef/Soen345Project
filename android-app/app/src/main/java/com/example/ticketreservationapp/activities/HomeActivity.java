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

public class HomeActivity extends BaseActivity {

    private TextView tvWelcome;
    private Button btnBrowseEvents, btnMyReservations, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvWelcome = findViewById(R.id.tvWelcome);
        btnBrowseEvents = findViewById(R.id.btnBrowseEvents);
        btnMyReservations = findViewById(R.id.btnMyReservations);
        btnLogout = findViewById(R.id.btnLogout);

        tvWelcome.setText("Welcome to Ticket Reservation App");

        btnBrowseEvents.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, EventListActivity.class);
            startActivity(intent);
        });

        btnMyReservations.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, MyReservationsActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logoutUser());
    }

    @Override
    protected void logoutUser() {
        btnLogout.setEnabled(false);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        apiService.logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                btnLogout.setEnabled(true);
                SessionManager sessionManager = new SessionManager(HomeActivity.this);
                sessionManager.clearSession();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                Toast.makeText(HomeActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnLogout.setEnabled(true);
                Toast.makeText(HomeActivity.this,
                        "Logout failed: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}