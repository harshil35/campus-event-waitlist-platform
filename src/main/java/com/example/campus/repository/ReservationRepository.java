package com.example.campus.repository;

import com.example.campus.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {
    long countByEventId(Long eventId);
    boolean existsByEventIdAndAttendeeEmail(
            Long eventId,
            String attendeeEmail
    );
    List<Reservation> findByEventId(Long eventId);
}