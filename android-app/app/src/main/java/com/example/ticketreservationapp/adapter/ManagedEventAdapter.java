package com.example.ticketreservationapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.model.Event;
import com.example.ticketreservationapp.utils.DateTimeUtils;

import java.util.List;

public class ManagedEventAdapter extends RecyclerView.Adapter<ManagedEventAdapter.ManagedEventViewHolder> {

    public interface OnEditClickListener {
        void onEditClick(Event event);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Event event);
    }

    private final List<Event> eventList;
    private final OnEditClickListener editListener;
    private final OnDeleteClickListener deleteListener;

    public ManagedEventAdapter(List<Event> eventList,
                               OnEditClickListener editListener,
                               OnDeleteClickListener deleteListener) {
        this.eventList = eventList;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ManagedEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_managed_event, parent, false);
        return new ManagedEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManagedEventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvManagedEventTitle.setText(event.getTitle());
        holder.tvManagedEventDate.setText("Date: " + DateTimeUtils.formatDateTime(event.getEventDate()));
        holder.tvManagedEventLocation.setText("Location: " + safe(event.getLocation()));
        holder.tvManagedEventSpots.setText("Spots: " + safeNum(event.getAvailableSpots()) + " / " + safeNum(event.getTotalSpots()));

        holder.btnEditManagedEvent.setOnClickListener(v -> editListener.onEditClick(event));
        holder.btnDeleteManagedEvent.setOnClickListener(v -> deleteListener.onDeleteClick(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class ManagedEventViewHolder extends RecyclerView.ViewHolder {
        TextView tvManagedEventTitle, tvManagedEventDate, tvManagedEventLocation, tvManagedEventSpots;
        Button btnEditManagedEvent, btnDeleteManagedEvent;

        public ManagedEventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvManagedEventTitle = itemView.findViewById(R.id.tvManagedEventTitle);
            tvManagedEventDate = itemView.findViewById(R.id.tvManagedEventDate);
            tvManagedEventLocation = itemView.findViewById(R.id.tvManagedEventLocation);
            tvManagedEventSpots = itemView.findViewById(R.id.tvManagedEventSpots);
            btnEditManagedEvent = itemView.findViewById(R.id.btnEditManagedEvent);
            btnDeleteManagedEvent = itemView.findViewById(R.id.btnDeleteManagedEvent);
        }
    }

    private String safe(String value) {
        return value != null ? value : "N/A";
    }

    private String safeNum(Integer value) {
        return value != null ? String.valueOf(value) : "N/A";
    }
}