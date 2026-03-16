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

        String title = (reservation.getEvent() != null && reservation.getEvent().getTitle() != null)
                ? reservation.getEvent().getTitle()
                : "N/A";

        String location = (reservation.getEvent() != null && reservation.getEvent().getLocation() != null)
                ? reservation.getEvent().getLocation()
                : "N/A";

        String eventDate = (reservation.getEvent() != null && reservation.getEvent().getEventDate() != null)
                ? reservation.getEvent().getEventDate()
                : null;

        holder.tvReservationTitle.setText(title);
        holder.tvReservationEventDate.setText("Event date: " + DateTimeUtils.formatDateTime(eventDate));
        holder.tvReservationDate.setText("Reserved on: " + DateTimeUtils.formatDateTime(reservation.getReservationDate()));
        holder.tvReservationStatus.setText("Status: " + safeText(reservation.getStatus()));
        holder.tvReservationLocation.setText("Location: " + location);

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