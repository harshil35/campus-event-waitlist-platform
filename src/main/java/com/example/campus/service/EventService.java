package com.example.campus.service;

import com.example.campus.dto.CreateEventRequest;
import com.example.campus.dto.UpdateEventRequest;
import com.example.campus.exception.EventNotFoundException;
import com.example.campus.exception.InvalidEventException;
import com.example.campus.model.Event;
import com.example.campus.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
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