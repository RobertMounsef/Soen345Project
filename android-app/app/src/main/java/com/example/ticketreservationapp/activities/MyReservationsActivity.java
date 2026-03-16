package com.example.ticketreservationapp.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.adapter.ReservationAdapter;
import com.example.ticketreservationapp.api.ApiService;
import com.example.ticketreservationapp.api.RetrofitClient;
import com.example.ticketreservationapp.model.Reservation;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyReservationsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewReservations;
    private TextView tvReservationsMessage;
    private ReservationAdapter reservationAdapter;
    private List<Reservation> reservationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservations);

        recyclerViewReservations = findViewById(R.id.recyclerViewReservations);
        tvReservationsMessage = findViewById(R.id.tvReservationsMessage);

        recyclerViewReservations.setLayoutManager(new LinearLayoutManager(this));

        reservationList = new ArrayList<>();
        reservationAdapter = new ReservationAdapter(reservationList, this::cancelReservation);
        recyclerViewReservations.setAdapter(reservationAdapter);

        loadReservations();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadReservations();
    }
    private void loadReservations() {
        tvReservationsMessage.setText("Loading reservations...");

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        apiService.getReservations().enqueue(new Callback<List<Reservation>>() {
            @Override
            public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    reservationList.clear();
                    reservationList.addAll(response.body());
                    reservationAdapter.notifyDataSetChanged();

                    if (reservationList.isEmpty()) {
                        tvReservationsMessage.setText("No reservations found.");
                    } else {
                        tvReservationsMessage.setText("");
                    }
                } else {
                    tvReservationsMessage.setText("Failed to load reservations.");
                    Toast.makeText(MyReservationsActivity.this,
                            "Could not load reservations",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Reservation>> call, Throwable t) {
                tvReservationsMessage.setText("Could not connect to backend.");
                Toast.makeText(MyReservationsActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void cancelReservation(Reservation reservation) {
        if (reservation.getReservationId() == null) {
            Toast.makeText(this, "Invalid reservation ID", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        apiService.cancelReservation(reservation.getReservationId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MyReservationsActivity.this,
                            "Reservation cancelled",
                            Toast.LENGTH_SHORT).show();
                    loadReservations();
                } else {
                    Toast.makeText(MyReservationsActivity.this,
                            "Cancellation failed",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MyReservationsActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}