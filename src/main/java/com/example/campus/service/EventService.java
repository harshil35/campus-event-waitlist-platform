package com.example.campus.service;

import com.example.campus.dto.CreateEventRequest;
import com.example.campus.dto.UpdateEventRequest;
import com.example.campus.exception.EventDeletionConflictException;
import com.example.campus.exception.EventNotFoundException;
import com.example.campus.exception.InvalidEventException;
import com.example.campus.model.Event;
import com.example.campus.repository.EventRepository;
import com.example.campus.repository.ReservationRepository;
import com.example.campus.repository.WaitlistRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final WaitlistRepository waitlistRepository;

    public EventService(EventRepository eventRepository,
                        ReservationRepository reservationRepository,
                        WaitlistRepository waitlistRepository) {
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.waitlistRepository = waitlistRepository;
    }


    public Event createEvent(CreateEventRequest request) {
        if (!request.getEndsAt().isAfter(request.getStartsAt())) {
            throw new InvalidEventException("endsAt must be after startsAt");
        }

        Event event = new Event();

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartsAt(request.getStartsAt());
        event.setEndsAt(request.getEndsAt());
        event.setVenue(request.getVenue());
        event.setCapacity(request.getCapacity());
        event.setOrganizerEmail(request.getOrganizerEmail());

        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, UpdateEventRequest request) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException("Event not found: " + id));

        if (!request.getEndsAt().isAfter(request.getStartsAt())) {
            throw new InvalidEventException(
                    "endsAt must be after startsAt");
        }

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartsAt(request.getStartsAt());
        event.setEndsAt(request.getEndsAt());
        event.setVenue(request.getVenue());
        event.setCapacity(request.getCapacity());
        event.setOrganizerEmail(request.getOrganizerEmail());

        return eventRepository.save(event);
    }

    public void deleteEvent(Long id) {

        Event event = eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException("Event not found: " + id));
        long reservationCount =
                reservationRepository.countByEventId(id);

        long waitlistCount =
                waitlistRepository.countByEventId(id);

        if (reservationCount > 0 || waitlistCount > 0) {
            throw new EventDeletionConflictException(
                    "Event cannot be deleted while it has reservations or waitlist entries");
        }
        eventRepository.delete(event);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() ->
                        new EventNotFoundException("Event not found: " + id));
    }
}