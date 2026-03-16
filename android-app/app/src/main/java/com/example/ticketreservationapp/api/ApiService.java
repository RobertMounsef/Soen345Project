package com.example.ticketreservationapp.api;

import com.example.ticketreservationapp.model.Event;
import com.example.ticketreservationapp.model.LoginRequest;
import com.example.ticketreservationapp.model.LoginResponse;
import com.example.ticketreservationapp.model.RegisterRequest;
import com.example.ticketreservationapp.model.RegisterResponse;
import com.example.ticketreservationapp.model.Reservation;
import com.example.ticketreservationapp.model.ReservationRequest;
import com.example.ticketreservationapp.model.AddEventRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.PUT;

public interface ApiService {

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("api/users")
    Call<RegisterResponse> register(@Body RegisterRequest registerRequest);

    @GET("api/events")
    Call<List<Event>> getEvents();

    @GET("api/events/{id}")
    Call<Event> getEventById(@Path("id") int eventId);

    @POST("api/events")
    Call<Event> createEvent(@Body AddEventRequest addEventRequest);

    @PUT("api/events/{id}")
    Call<Event> updateEvent(@Path("id") int eventId, @Body AddEventRequest request);

    @DELETE("api/events/{id}")
    Call<Void> deleteEvent(@Path("id") int eventId);
    @POST("api/reservations")
    Call<Void> reserveEvent(@Body ReservationRequest reservationRequest);

    @GET("api/reservations")
    Call<List<Reservation>> getReservations();

    @DELETE("api/reservations/{id}")
    Call<Void> cancelReservation(@Path("id") int reservationId);

    @POST("api/auth/logout")
    Call<Void> logout();


}