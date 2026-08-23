package com.example.campus.controller;

import com.example.campus.dto.*;
import com.example.campus.model.Event;
import com.example.campus.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(
            @Valid @RequestBody CreateEventRequest request) {

        Event event = eventService.createEvent(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(event);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request) {

        Event event = eventService.updateEvent(id, request);

        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {

        eventService.deleteEvent(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {

        List<Event> events = eventService.getAllEvents();

        return ResponseEntity.ok(events);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {

        Event event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }
}