package com.example.ticketreservationapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.model.Reservation;
import com.example.ticketreservationapp.utils.DateTimeUtils;

import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    public interface OnCancelClickListener {
        void onCancelClick(Reservation reservation);
    }

    private final List<Reservation> reservationList;
    private final OnCancelClickListener listener;

    public ReservationAdapter(List<Reservation> reservationList, OnCancelClickListener listener) {
        this.reservationList = reservationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);

        // Show legible event info instead of raw IDs
        String title = reservation.getEventTitle() != null ? reservation.getEventTitle() : "Event ID: " + reservation.getEventId();
        String eventDate = reservation.getEventDate() != null ? DateTimeUtils.formatDateTime(reservation.getEventDate()) : "";
        String location = reservation.getEventLocation() != null ? reservation.getEventLocation() : "";

        holder.tvReservationTitle.setText(title);
        holder.tvReservationEventDate.setText("Event Date: " + eventDate);
        holder.tvReservationDate.setText("Reserved on: " + DateTimeUtils.formatDateTime(reservation.getReservationDate()));
        String status = safeText(reservation.getStatus());
        holder.tvReservationStatus.setText("Status: " + status);
        holder.tvReservationLocation.setText("Location: " + location);

        if ("REMOVED_BY_ORGANIZER".equalsIgnoreCase(status)) {
            holder.tvReservationStatus.setTextColor(android.graphics.Color.RED);
            holder.tvReservationStatus.setText("Status: REMOVED BY ORGANIZER");
            holder.btnCancelReservation.setVisibility(View.GONE);
        } else if ("CANCELLED".equalsIgnoreCase(status)) {
            holder.tvReservationStatus.setTextColor(android.graphics.Color.GRAY);
            holder.btnCancelReservation.setVisibility(View.GONE);
        } else {
            holder.tvReservationStatus.setTextColor(android.graphics.Color.BLACK);
            holder.btnCancelReservation.setVisibility(View.VISIBLE);
        }

        holder.btnCancelReservation.setOnClickListener(v -> listener.onCancelClick(reservation));
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView tvReservationTitle, tvReservationEventDate, tvReservationDate, tvReservationStatus, tvReservationLocation;
        Button btnCancelReservation;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReservationTitle = itemView.findViewById(R.id.tvReservationTitle);
            tvReservationEventDate = itemView.findViewById(R.id.tvReservationEventDate);
            tvReservationDate = itemView.findViewById(R.id.tvReservationDate);
            tvReservationStatus = itemView.findViewById(R.id.tvReservationStatus);
            tvReservationLocation = itemView.findViewById(R.id.tvReservationLocation);
            btnCancelReservation = itemView.findViewById(R.id.btnCancelReservation);
        }
    }

    private String safeText(String value) {
        return value != null ? value : "N/A";
    }
}