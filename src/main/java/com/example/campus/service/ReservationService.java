package com.example.campus.service;

import com.example.campus.exception.ReservationNotFoundException;
import com.example.campus.model.WaitlistEntry;
import com.example.campus.repository.EventRepository;
import com.example.campus.repository.ReservationRepository;
import com.example.campus.repository.WaitlistRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.example.campus.dto.CreateReservationRequest;
import com.example.campus.exception.EventNotFoundException;
import com.example.campus.exception.InvalidReservationException;
import com.example.campus.model.Event;
import com.example.campus.model.Reservation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final WaitlistRepository waitlistRepository;

    public ReservationService(
            EventRepository eventRepository,
            ReservationRepository reservationRepository,
            WaitlistRepository waitlistRepository) {

        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.waitlistRepository = waitlistRepository;
    }

    public Reservation getReservation(
            Long eventId,
            Long reservationId) {

        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() ->
                        new ReservationNotFoundException(
                                "Reservation not found: " + reservationId));

        if (!reservation.getEvent().getId().equals(eventId)) {
            throw new ReservationNotFoundException(
                    "Reservation does not belong to event: " + eventId);
        }

        return reservation;
    }

    public List<Reservation> getReservationsForEvent(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new EventNotFoundException(
                    "Event not found: " + eventId);
        }
        return reservationRepository.findByEventId(eventId);
    }

    @Transactional
    public Reservation createReservation(
            Long eventId,
            CreateReservationRequest request) {

        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() ->
                        new EventNotFoundException(
                                "Event not found: " + eventId));

        boolean alreadyReserved =
                reservationRepository.existsByEventIdAndAttendeeEmail(
                        eventId,
                        request.getAttendeeEmail());

        if (alreadyReserved) {
            throw new InvalidReservationException(
                    "Attendee has already reserved this event");
        }

        long reservationCount =
                reservationRepository.countByEventId(eventId);

        if (reservationCount >= event.getCapacity()) {
            throw new InvalidReservationException(
                    "Event is full");
        }

        Reservation reservation = new Reservation();

        reservation.setEvent(event);
        reservation.setAttendeeEmail(request.getAttendeeEmail());
        reservation.setCreatedAt(LocalDateTime.now());

        return reservationRepository.save(reservation);
    }

    @Transactional
    public void cancelReservation(Long eventId, Long reservationId) {

        Event event = eventRepository.findByIdForUpdate(eventId)
                .orElseThrow(() ->
                        new EventNotFoundException(
                                "Event not found: " + eventId));

        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() ->
                        new ReservationNotFoundException(
                                "Reservation not found: " + reservationId));

        if (!reservation.getEvent().getId().equals(eventId)) {
            throw new ReservationNotFoundException(
                    "Reservation does not belong to event: " + eventId);
        }

        reservationRepository.delete(reservation);

        Optional<WaitlistEntry> nextEntry =
                waitlistRepository.findFirstByEventIdOrderByCreatedAtAsc(eventId);

        if (nextEntry.isPresent()) {
            WaitlistEntry waitlistEntry = nextEntry.get();
            Reservation promotedReservation = new Reservation();
            promotedReservation.setEvent(event);
            promotedReservation.setAttendeeEmail(
                    waitlistEntry.getAttendeeEmail());
            promotedReservation.setCreatedAt(LocalDateTime.now());

            reservationRepository.save(promotedReservation);

            waitlistRepository.delete(waitlistEntry);
        }
    }
}