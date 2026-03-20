package com.example.ticketreservationapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.adapter.ManagedEventAdapter;
import com.example.ticketreservationapp.api.ApiService;
import com.example.ticketreservationapp.api.RetrofitClient;
import com.example.ticketreservationapp.model.Event;
import com.example.ticketreservationapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagedEventsActivity extends BaseActivity {

    private RecyclerView recyclerViewManagedEvents;
    private TextView tvManagedEventsMessage;
    private ManagedEventAdapter adapter;
    private List<Event> managedEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managed_events);

        recyclerViewManagedEvents = findViewById(R.id.recyclerViewManagedEvents);
        tvManagedEventsMessage = findViewById(R.id.tvManagedEventsMessage);

        recyclerViewManagedEvents.setLayoutManager(new LinearLayoutManager(this));

        managedEvents = new ArrayList<>();
        adapter = new ManagedEventAdapter(managedEvents, this::editEvent, this::deleteEvent);
        recyclerViewManagedEvents.setAdapter(adapter);

        loadManagedEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadManagedEvents();
    }

    private void loadManagedEvents() {
        tvManagedEventsMessage.setText("Loading events...");

        SessionManager sessionManager = new SessionManager(this);
        int currentUserId = sessionManager.getUserId();

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getEvents().enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    managedEvents.clear();

                    for (Event event : response.body()) {
                        if (event.getOrganizer() != null
                                && event.getOrganizer().getUserId() != null
                                && event.getOrganizer().getUserId() == currentUserId) {
                            managedEvents.add(event);
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (managedEvents.isEmpty()) {
                        tvManagedEventsMessage.setText("No managed events found.");
                    } else {
                        tvManagedEventsMessage.setText("");
                    }
                } else {
                    tvManagedEventsMessage.setText("Failed to load events.");
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                tvManagedEventsMessage.setText("Could not connect to backend.");
                Toast.makeText(ManagedEventsActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void editEvent(Event event) {
        Intent intent = new Intent(this, AddEditEventActivity.class);
        intent.putExtra("isEdit", true);
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

    private void deleteEvent(Event event) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.deleteEvent(event.getEventId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ManagedEventsActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                    loadManagedEvents();
                } else {
                    Toast.makeText(ManagedEventsActivity.this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ManagedEventsActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}