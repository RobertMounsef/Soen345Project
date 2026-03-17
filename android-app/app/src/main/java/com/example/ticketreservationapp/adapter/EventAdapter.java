package com.example.ticketreservationapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.ticketreservationapp.utils.DateTimeUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketreservationapp.R;
import com.example.ticketreservationapp.model.Event;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    private final List<Event> eventList;
    private final OnEventClickListener listener;

    public EventAdapter(List<Event> eventList, OnEventClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.tvEventTitle.setText(event.getTitle());
        holder.tvEventCategory.setText("Category: " + safeText(event.getCategory()));
        holder.tvEventDate.setText("Date: " + DateTimeUtils.formatDateTime(event.getEventDate()));        holder.tvEventLocation.setText("Location: " + safeText(event.getLocation()));
        holder.tvEventSpots.setText("Available Spots: " + safeNumber(event.getAvailableSpots())
                + " / " + safeNumber(event.getTotalSpots()));
        holder.tvEventStatus.setText("Status: " + safeText(event.getStatus()));

        holder.itemView.setOnClickListener(v -> listener.onEventClick(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventTitle, tvEventCategory, tvEventDate, tvEventLocation, tvEventSpots, tvEventStatus;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventTitle = itemView.findViewById(R.id.tvEventTitle);
            tvEventCategory = itemView.findViewById(R.id.tvEventCategory);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            tvEventLocation = itemView.findViewById(R.id.tvEventLocation);
            tvEventSpots = itemView.findViewById(R.id.tvEventSpots);
            tvEventStatus = itemView.findViewById(R.id.tvEventStatus);
        }
    }

    private String safeText(String value) {
        return value != null ? value : "N/A";
    }

    private String safeNumber(Integer value) {
        return value != null ? String.valueOf(value) : "N/A";
    }
}