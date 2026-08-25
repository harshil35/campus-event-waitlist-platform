package com.example.campus.controller;

import com.example.campus.dto.CreateReservationRequest;
import com.example.campus.exception.RateLimitExceededException;
import com.example.campus.model.Reservation;
import com.example.campus.service.RateLimitService;
import com.example.campus.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events/{eventId}/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final RateLimitService rateLimitService;

    public ReservationController(
            ReservationService reservationService,
            RateLimitService rateLimitService) {
        this.reservationService = reservationService;
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<Reservation> getReservation(
            @PathVariable Long eventId,
            @PathVariable Long reservationId) {

        Reservation reservation =
                reservationService.getReservation(
                        eventId,
                        reservationId);

        return ResponseEntity.ok(reservation);
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getReservationsForEvent(
            @PathVariable Long eventId) {

        List<Reservation> reservations =
                reservationService.getReservationsForEvent(eventId);

        return ResponseEntity.ok(reservations);
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateReservationRequest request) {

        if (!rateLimitService.isAllowed(request.getAttendeeEmail())) {
            throw new RateLimitExceededException();
        }

        Reservation reservation =
                reservationService.createReservation(eventId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservation);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long eventId,
            @PathVariable Long reservationId) {

        reservationService.cancelReservation(eventId, reservationId);

        return ResponseEntity.noContent().build();
    }
}