package com.example.ticketreservationapp.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.api.ApiService;
import com.example.ticketreservationapp.api.RetrofitClient;
import com.example.ticketreservationapp.model.AddEventRequest;
import com.example.ticketreservationapp.model.Event;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddEditEventActivity extends AppCompatActivity {

    private EditText etEventTitle, etEventCategory, etEventDateTime, etEventLocation, etTotalSpots;
    private Button btnCreateEvent;
    private TextView tvAddEventMessage;

    private boolean isEdit = false;
    private int eventId = -1;
    private int availableSpots = 0;
    private String status = "ACTIVE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_event);

        etEventTitle = findViewById(R.id.etEventTitle);
        etEventCategory = findViewById(R.id.etEventCategory);
        etEventDateTime = findViewById(R.id.etEventDateTime);
        etEventLocation = findViewById(R.id.etEventLocation);
        etTotalSpots = findViewById(R.id.etTotalSpots);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        tvAddEventMessage = findViewById(R.id.tvAddEventMessage);

        isEdit = getIntent().getBooleanExtra("isEdit", false);

        if (isEdit) {
            eventId = getIntent().getIntExtra("eventId", -1);
            availableSpots = getIntent().getIntExtra("availableSpots", 0);
            status = getIntent().getStringExtra("status");

            etEventTitle.setText(getIntent().getStringExtra("title"));
            etEventCategory.setText(getIntent().getStringExtra("category"));
            etEventDateTime.setText(getIntent().getStringExtra("eventDate"));
            etEventLocation.setText(getIntent().getStringExtra("location"));
            etTotalSpots.setText(String.valueOf(getIntent().getIntExtra("totalSpots", 0)));

            btnCreateEvent.setText("Update Event");
            tvAddEventMessage.setText("Edit event details");
        }

        btnCreateEvent.setOnClickListener(v -> {
            if (isEdit) {
                updateEvent();
            } else {
                createEvent();
            }
        });
    }

    private void createEvent() {
        submitEventRequest(false);
    }

    private void updateEvent() {
        submitEventRequest(true);
    }

    private void submitEventRequest(boolean editing) {
        String title = etEventTitle.getText().toString().trim();
        String category = etEventCategory.getText().toString().trim();
        String eventDate = etEventDateTime.getText().toString().trim();
        String location = etEventLocation.getText().toString().trim();
        String totalSpotsText = etTotalSpots.getText().toString().trim();

        if (title.isEmpty() || category.isEmpty() || eventDate.isEmpty()
                || location.isEmpty() || totalSpotsText.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalSpots;
        try {
            totalSpots = Integer.parseInt(totalSpotsText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Total spots must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        int newAvailableSpots = editing ? Math.min(availableSpots, totalSpots) : totalSpots;

        AddEventRequest request = new AddEventRequest(
                title,
                category,
                eventDate,
                location,
                totalSpots,
                newAvailableSpots,
                status != null ? status : "ACTIVE"
        );

        btnCreateEvent.setEnabled(false);
        tvAddEventMessage.setText(editing ? "Updating event..." : "Creating event...");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Callback<Event> callback = new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                btnCreateEvent.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AddEditEventActivity.this,
                            editing ? "Event updated successfully" : "Event created successfully",
                            Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    tvAddEventMessage.setText(editing ? "Failed to update event." : "Failed to create event.");
                }
            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {
                btnCreateEvent.setEnabled(true);
                tvAddEventMessage.setText("Could not connect to backend.");
                Toast.makeText(AddEditEventActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        };

        if (editing) {
            apiService.updateEvent(eventId, request).enqueue(callback);
        } else {
            apiService.createEvent(request).enqueue(callback);
        }
    }
}