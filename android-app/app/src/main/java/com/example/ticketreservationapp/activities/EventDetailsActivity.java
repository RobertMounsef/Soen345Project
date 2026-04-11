package com.example.ticketreservationapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.api.ApiService;
import com.example.ticketreservationapp.api.RetrofitClient;
import com.example.ticketreservationapp.model.Event;
import com.example.ticketreservationapp.model.ReservationRequest;
import com.example.ticketreservationapp.utils.DateTimeUtils;
import com.example.ticketreservationapp.utils.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventDetailsActivity extends BaseActivity {

    private String eventId;  // Firebase push key (String)
    private TextView tvTitle, tvCategory, tvDate, tvLocation, tvSpots, tvStatus, tvReserveMessage;
    private Button btnReserve;
    private boolean isAlreadyReserved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        tvTitle = findViewById(R.id.tvDetailTitle);
        tvCategory = findViewById(R.id.tvDetailCategory);
        tvDate = findViewById(R.id.tvDetailDate);
        tvLocation = findViewById(R.id.tvDetailLocation);
        tvSpots = findViewById(R.id.tvDetailSpots);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvReserveMessage = findViewById(R.id.tvReserveMessage);
        btnReserve = findViewById(R.id.btnReserveEvent);

        eventId = getIntent().getStringExtra("eventId");  // String, not int

        btnReserve.setOnClickListener(v -> reserveEvent());

        loadEventDetails();
    }

    private void loadEventDetails() {
        if (eventId == null || eventId.isEmpty()) {
            tvReserveMessage.setText("Invalid event.");
            btnReserve.setEnabled(false);
            return;
        }

        tvReserveMessage.setText("Loading event details...");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        SessionManager sessionManager = new SessionManager(EventDetailsActivity.this);
        String currentRole = sessionManager.getRole();
        if ("USER".equalsIgnoreCase(currentRole) || "CUSTOMER".equalsIgnoreCase(currentRole)) {
            apiService.getReservations().enqueue(new Callback<java.util.List<com.example.ticketreservationapp.model.Reservation>>() {
                @Override
                public void onResponse(Call<java.util.List<com.example.ticketreservationapp.model.Reservation>> call, Response<java.util.List<com.example.ticketreservationapp.model.Reservation>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (com.example.ticketreservationapp.model.Reservation res : response.body()) {
                            // Compare flat eventId String
                            if (eventId.equals(res.getEventId())) {
                                isAlreadyReserved = true;
                                break;
                            }
                        }
                    }
                    fetchEventData(apiService);
                }

                @Override
                public void onFailure(Call<java.util.List<com.example.ticketreservationapp.model.Reservation>> call, Throwable t) {
                    fetchEventData(apiService);
                }
            });
        } else {
            fetchEventData(apiService);
        }
    }

    private void fetchEventData(ApiService apiService) {
        apiService.getEventById(eventId).enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Event event = response.body();

                    tvTitle.setText(safe(event.getTitle()));
                    tvCategory.setText("Category: " + safe(event.getCategory()));
                    tvDate.setText("Date: " + DateTimeUtils.formatDateTime(event.getEventDate()));
                    tvLocation.setText("Location: " + safe(event.getLocation()));
                    tvSpots.setText("Available Spots: " + safeNum(event.getAvailableSpots())
                            + " / " + safeNum(event.getTotalSpots()));
                    tvStatus.setText("Status: " + safe(event.getStatus()));
                    tvReserveMessage.setText("");

                    SessionManager sessionManager = new SessionManager(EventDetailsActivity.this);
                    String currentRole = sessionManager.getRole();

                    if ("ORGANIZER".equalsIgnoreCase(currentRole) || "ADMIN".equalsIgnoreCase(currentRole)) {
                        btnReserve.setEnabled(false);
                        btnReserve.setVisibility(android.view.View.GONE);
                        tvReserveMessage.setText("Organizers cannot reserve events.");
                    } else if (isAlreadyReserved) {
                        btnReserve.setVisibility(android.view.View.GONE);
                        tvReserveMessage.setText(
                                "Reserved! Confirmation sent by email and/or SMS (based on your account).");
                    } else {
                        boolean canReserve =
                                event.getAvailableSpots() != null &&
                                        event.getAvailableSpots() > 0 &&
                                        "ACTIVE".equalsIgnoreCase(safe(event.getStatus()));

                        btnReserve.setEnabled(canReserve);
                        btnReserve.setVisibility(canReserve ? android.view.View.VISIBLE : android.view.View.GONE);

                        if (!canReserve) {
                            tvReserveMessage.setText("This event is not available for reservation.");
                        } else {
                            tvReserveMessage.setText("");
                        }
                    }
                } else {
                    tvReserveMessage.setText("Failed to load event details.");
                    btnReserve.setEnabled(false);
                }
            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {
                tvReserveMessage.setText("Could not connect to backend.");
                btnReserve.setEnabled(false);
            }
        });
    }

    private void reserveEvent() {
        if (eventId == null || eventId.isEmpty()) {
            tvReserveMessage.setText("Invalid event.");
            return;
        }

        btnReserve.setEnabled(false);
        tvReserveMessage.setText("Submitting reservation...");

        ReservationRequest request = new ReservationRequest(eventId);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        apiService.reserveEvent(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    isAlreadyReserved = true;
                    Toast.makeText(EventDetailsActivity.this,
                            "Reservation successful! Check your email or SMS for confirmation.",
                            Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    loadEventDetails();
                } else if (response.code() == 409) {
                    isAlreadyReserved = true;
                    Toast.makeText(EventDetailsActivity.this,
                            "You already reserved this event",
                            Toast.LENGTH_SHORT).show();
                    loadEventDetails();
                } else {
                    btnReserve.setEnabled(true);
                    tvReserveMessage.setText("Reservation failed.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                btnReserve.setEnabled(true);
                tvReserveMessage.setText("Could not connect to backend.");
            }
        });
    }

    private String safe(String value) {
        return value != null ? value : "N/A";
    }

    private String safeNum(Integer value) {
        return value != null ? String.valueOf(value) : "N/A";
    }
}