package com.example.campus.repository;

import com.example.campus.model.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WaitlistRepository
        extends JpaRepository<WaitlistEntry, Long> {
    long countByEventId(Long eventId);
    boolean existsByEventIdAndAttendeeEmail(
            Long eventId,
            String attendeeEmail
    );
    Optional<WaitlistEntry> findFirstByEventIdOrderByCreatedAtAsc(
            Long eventId
    );
    List<WaitlistEntry> findByEventIdOrderByCreatedAtAsc(Long eventId);
}