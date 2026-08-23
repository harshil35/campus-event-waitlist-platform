package com.example.campus.controller;

import com.example.campus.dto.CreateReservationRequest;
import com.example.campus.model.Reservation;
import com.example.campus.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events/{eventId}/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(
            ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateReservationRequest request) {

        Reservation reservation =
                reservationService.createReservation(eventId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservation);
    }
}