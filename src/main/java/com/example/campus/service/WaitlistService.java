package com.example.campus.service;

import com.example.campus.dto.CreateWaitlistEntryRequest;
import com.example.campus.exception.EventNotFoundException;
import com.example.campus.exception.InvalidReservationException;
import com.example.campus.exception.WaitlistEntryNotFoundException;
import com.example.campus.model.Event;
import com.example.campus.model.WaitlistEntry;
import com.example.campus.repository.EventRepository;
import com.example.campus.repository.ReservationRepository;
import com.example.campus.repository.WaitlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import java.time.LocalDateTime;

@Service
public class WaitlistService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final WaitlistRepository waitlistRepository;

    public WaitlistService(
            EventRepository eventRepository,
            ReservationRepository reservationRepository,
            WaitlistRepository waitlistRepository) {

        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.waitlistRepository = waitlistRepository;
    }

    public List<WaitlistEntry> getWaitlist(Long eventId) {

        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException(
                    "Event not found: " + eventId);
        }

        return waitlistRepository
                .findByEventIdOrderByCreatedAtAsc(eventId);
    }

    public WaitlistEntry getWaitlistEntry(
            Long eventId,
            Long entryId) {

        WaitlistEntry entry = waitlistRepository
                .findById(entryId)
                .orElseThrow(() ->
                        new WaitlistEntryNotFoundException(
                                "Waitlist entry not found: " + entryId));

        if (!entry.getEvent().getId().equals(eventId)) {
            throw new WaitlistEntryNotFoundException(
                    "Waitlist entry does not belong to event: " + eventId);
        }

        return entry;
    }

    @Transactional
    public void removeFromWaitlist(
            Long eventId,
            Long entryId) {

        WaitlistEntry entry = waitlistRepository
                .findById(entryId)
                .orElseThrow(() ->
                        new WaitlistEntryNotFoundException(
                                "Waitlist entry not found: " + entryId));

        if (!entry.getEvent().getId().equals(eventId)) {
            throw new WaitlistEntryNotFoundException(
                    "Waitlist entry does not belong to event: " + eventId);
        }

        waitlistRepository.delete(entry);
    }

    @Transactional
    public WaitlistEntry joinWaitlist(
            Long eventId,
            CreateWaitlistEntryRequest request) {

        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() ->
                        new EventNotFoundException(
                                "Event not found: " + eventId));

        long reservationCount =
                reservationRepository.countByEventId(eventId);

        if (reservationCount < event.getCapacity()) {
            throw new InvalidReservationException(
                    "Event still has available seats");
        }

        boolean alreadyReserved =
                reservationRepository.existsByEventIdAndAttendeeEmail(
                        eventId,
                        request.getAttendeeEmail());

        if (alreadyReserved) {
            throw new InvalidReservationException(
                    "Attendee already has a reservation");
        }

        boolean alreadyWaitlisted =
                waitlistRepository.existsByEventIdAndAttendeeEmail(
                        eventId,
                        request.getAttendeeEmail());

        if (alreadyWaitlisted) {
            throw new InvalidReservationException(
                    "Attendee is already on the waitlist");
        }

        WaitlistEntry entry = new WaitlistEntry();

        entry.setEvent(event);
        entry.setAttendeeEmail(request.getAttendeeEmail());
        entry.setCreatedAt(LocalDateTime.now());

        return waitlistRepository.save(entry);
    }
}