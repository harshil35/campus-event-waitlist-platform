package com.example.campus.repository;

import com.example.campus.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {
    long countByEventId(Long eventId);
    boolean existsByEventIdAndAttendeeEmail(
            Long eventId,
            String attendeeEmail
    );
}