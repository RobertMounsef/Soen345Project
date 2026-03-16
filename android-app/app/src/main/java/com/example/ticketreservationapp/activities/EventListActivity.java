package com.example.ticketreservationapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ticketreservationapp.utils.DateTimeUtils;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.adapter.EventAdapter;
import com.example.ticketreservationapp.api.ApiService;
import com.example.ticketreservationapp.api.RetrofitClient;
import com.example.ticketreservationapp.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewEvents;
    private TextView tvEventMessage;
    private EditText etSearchEvents;

    private EventAdapter eventAdapter;
    private List<Event> displayedEvents;
    private List<Event> allEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        recyclerViewEvents = findViewById(R.id.recyclerViewEvents);
        tvEventMessage = findViewById(R.id.tvEventMessage);
        etSearchEvents = findViewById(R.id.etSearchEvents);

        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));

        displayedEvents = new ArrayList<>();
        allEvents = new ArrayList<>();

        eventAdapter = new EventAdapter(displayedEvents, this::openEventDetails);
        recyclerViewEvents.setAdapter(eventAdapter);

        etSearchEvents.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        loadEvents();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }
    private void loadEvents() {
        tvEventMessage.setText("Loading events...");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        apiService.getEvents().enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allEvents.clear();
                    allEvents.addAll(response.body());

                    filterEvents(etSearchEvents.getText().toString());
                } else {
                    tvEventMessage.setText("Failed to load events.");
                    Toast.makeText(EventListActivity.this,
                            "Could not load events",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                tvEventMessage.setText("Could not connect to backend.");
                Toast.makeText(EventListActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterEvents(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

        displayedEvents.clear();

        if (q.isEmpty()) {
            displayedEvents.addAll(allEvents);
        } else {
            for (Event event : allEvents) {
                String title = safe(event.getTitle()).toLowerCase(Locale.ROOT);
                String category = safe(event.getCategory()).toLowerCase(Locale.ROOT);
                String location = safe(event.getLocation()).toLowerCase(Locale.ROOT);
                String rawEventDate = safe(event.getEventDate()).toLowerCase(Locale.ROOT);
                String formattedEventDate = DateTimeUtils.formatDateTime(event.getEventDate()).toLowerCase(Locale.ROOT);

                if (title.contains(q) || category.contains(q) || location.contains(q)
                        || rawEventDate.contains(q) || formattedEventDate.contains(q)) {
                    displayedEvents.add(event);
                }
            }
        }

        eventAdapter.notifyDataSetChanged();

        if (allEvents.isEmpty()) {
            tvEventMessage.setText("No events found.");
        } else if (displayedEvents.isEmpty()) {
            tvEventMessage.setText("No matching events.");
        } else {
            tvEventMessage.setText("Tap an event to view details.");
        }
    }

    private void openEventDetails(Event event) {
        Intent intent = new Intent(EventListActivity.this, EventDetailsActivity.class);
        intent.putExtra("eventId", event.getEventId());
        intent.putExtra("title", event.getTitle());
        intent.putExtra("category", event.getCategory());
        intent.putExtra("eventDate", event.getEventDate());
        intent.putExtra("location", event.getLocation());
        intent.putExtra("totalSpots", event.getTotalSpots());
        intent.putExtra("availableSpots", event.getAvailableSpots());
        intent.putExtra("status", event.getStatus());
        startActivity(intent);
    }

    private String safe(String value) {
        return value != null ? value : "";
    }
}