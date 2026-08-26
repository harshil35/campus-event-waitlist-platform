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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private WaitlistRepository waitlistRepository;

    @InjectMocks
    private EventService eventService;

    private CreateEventRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new CreateEventRequest();

        createRequest.setTitle("Spring Boot Workshop");
        createRequest.setDescription("Learn Spring Boot");
        createRequest.setStartsAt(
                LocalDateTime.of(2026, 8, 26, 10, 0));
        createRequest.setEndsAt(
                LocalDateTime.of(2026, 8, 26, 12, 0));
        createRequest.setVenue("Room 101");
        createRequest.setCapacity(50);
        createRequest.setOrganizerEmail("teacher@example.com");
    }

    @Test
    void createEvent_shouldSaveAndReturnEvent() {

        Event savedEvent = new Event();
        savedEvent.setId(1L);
        savedEvent.setTitle("Spring Boot Workshop");
        savedEvent.setDescription("Learn Spring Boot");
        savedEvent.setStartsAt(createRequest.getStartsAt());
        savedEvent.setEndsAt(createRequest.getEndsAt());
        savedEvent.setVenue("Room 101");
        savedEvent.setCapacity(50);
        savedEvent.setOrganizerEmail("teacher@example.com");

        when(eventRepository.save(any(Event.class)))
                .thenReturn(savedEvent);

        Event result = eventService.createEvent(createRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(
                "Spring Boot Workshop",
                result.getTitle());
        assertEquals(50, result.getCapacity());

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEvent_shouldRejectInvalidDates() {

        createRequest.setEndsAt(
                LocalDateTime.of(2026, 8, 26, 9, 0));

        InvalidEventException exception =
                assertThrows(
                        InvalidEventException.class,
                        () -> eventService.createEvent(createRequest));

        assertEquals(
                "endsAt must be after startsAt",
                exception.getMessage());

        verify(eventRepository, never())
                .save(any(Event.class));
    }

    @Test
    void getAllEvents_shouldReturnAllEvents() {

        Event event1 = new Event();
        event1.setId(1L);
        event1.setTitle("Spring Boot Workshop");

        Event event2 = new Event();
        event2.setId(2L);
        event2.setTitle("Java Workshop");

        when(eventRepository.findAll())
                .thenReturn(List.of(event1, event2));

        List<Event> result = eventService.getAllEvents();

        assertEquals(2, result.size());
        assertEquals("Spring Boot Workshop",
                result.get(0).getTitle());
        assertEquals("Java Workshop",
                result.get(1).getTitle());

        verify(eventRepository).findAll();
    }

    @Test
    void getEventById_shouldReturnEventWhenFound() {

        Event event = new Event();
        event.setId(1L);
        event.setTitle("Spring Boot Workshop");

        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        Event result = eventService.getEventById(1L);

        assertEquals(1L, result.getId());
        assertEquals(
                "Spring Boot Workshop",
                result.getTitle());

        verify(eventRepository).findById(1L);
    }

    @Test
    void getEventById_shouldThrowWhenEventDoesNotExist() {

        when(eventRepository.findById(999L))
                .thenReturn(Optional.empty());

        EventNotFoundException exception =
                assertThrows(
                        EventNotFoundException.class,
                        () -> eventService.getEventById(999L));

        assertEquals(
                "Event not found: 999",
                exception.getMessage());

        verify(eventRepository).findById(999L);
    }

    @Test
    void updateEvent_shouldUpdateAndSave() {

        Event existingEvent = new Event();
        existingEvent.setId(1L);
        existingEvent.setTitle("Old Title");
        existingEvent.setDescription("Old Description");
        existingEvent.setStartsAt(
                LocalDateTime.of(2026, 8, 26, 10, 0));
        existingEvent.setEndsAt(
                LocalDateTime.of(2026, 8, 26, 12, 0));
        existingEvent.setVenue("Old Room");
        existingEvent.setCapacity(30);
        existingEvent.setOrganizerEmail("old@example.com");

        UpdateEventRequest updateRequest = new UpdateEventRequest();
        updateRequest.setTitle("Updated Workshop");
        updateRequest.setDescription("Updated Description");
        updateRequest.setStartsAt(
                LocalDateTime.of(2026, 8, 26, 14, 0));
        updateRequest.setEndsAt(
                LocalDateTime.of(2026, 8, 26, 16, 0));
        updateRequest.setVenue("Room 202");
        updateRequest.setCapacity(50);
        updateRequest.setOrganizerEmail("new@example.com");

        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(existingEvent));

        when(eventRepository.save(existingEvent))
                .thenReturn(existingEvent);

        Event result =
                eventService.updateEvent(1L, updateRequest);

        assertEquals("Updated Workshop", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("Room 202", result.getVenue());
        assertEquals(50, result.getCapacity());
        assertEquals(
                "new@example.com",
                result.getOrganizerEmail());

        verify(eventRepository).findById(1L);
        verify(eventRepository).save(existingEvent);
    }

    @Test
    void updateEvent_shouldRejectInvalidDates() {

        Event existingEvent = new Event();
        existingEvent.setId(1L);
        existingEvent.setTitle("Existing Event");

        UpdateEventRequest updateRequest = new UpdateEventRequest();
        updateRequest.setTitle("Updated Event");
        updateRequest.setStartsAt(
                LocalDateTime.of(2026, 8, 26, 10, 0));
        updateRequest.setEndsAt(
                LocalDateTime.of(2026, 8, 26, 9, 0));

        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(existingEvent));

        InvalidEventException exception =
                assertThrows(
                        InvalidEventException.class,
                        () -> eventService.updateEvent(
                                1L,
                                updateRequest));

        assertEquals(
                "endsAt must be after startsAt",
                exception.getMessage());

        verify(eventRepository, never())
                .save(any(Event.class));
    }

    @Test
    void updateEvent_shouldThrowWhenEventDoesNotExist() {

        UpdateEventRequest updateRequest = new UpdateEventRequest();

        when(eventRepository.findById(999L))
                .thenReturn(Optional.empty());

        EventNotFoundException exception =
                assertThrows(
                        EventNotFoundException.class,
                        () -> eventService.updateEvent(
                                999L,
                                updateRequest));

        assertEquals(
                "Event not found: 999",
                exception.getMessage());

        verify(eventRepository).findById(999L);
        verify(eventRepository, never())
                .save(any(Event.class));
    }

    @Test
    void deleteEvent_shouldDeleteWhenNoReservationsOrWaitlist() {

        Event event = new Event();
        event.setId(1L);
        event.setTitle("Spring Boot Workshop");

        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        when(reservationRepository.countByEventId(1L))
                .thenReturn(0L);

        when(waitlistRepository.countByEventId(1L))
                .thenReturn(0L);

        eventService.deleteEvent(1L);

        verify(eventRepository).findById(1L);
        verify(reservationRepository).countByEventId(1L);
        verify(waitlistRepository).countByEventId(1L);
        verify(eventRepository).delete(event);
    }

    @Test
    void deleteEvent_shouldRejectWhenReservationsExist() {

        Event event = new Event();
        event.setId(1L);

        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        when(reservationRepository.countByEventId(1L))
                .thenReturn(2L);

        when(waitlistRepository.countByEventId(1L))
                .thenReturn(0L);

        EventDeletionConflictException exception =
                assertThrows(
                        EventDeletionConflictException.class,
                        () -> eventService.deleteEvent(1L));

        assertEquals(
                "Event cannot be deleted while it has reservations or waitlist entries",
                exception.getMessage());

        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    void deleteEvent_shouldRejectWhenWaitlistExists() {

        Event event = new Event();
        event.setId(1L);

        when(eventRepository.findById(1L))
                .thenReturn(Optional.of(event));

        when(reservationRepository.countByEventId(1L))
                .thenReturn(0L);

        when(waitlistRepository.countByEventId(1L))
                .thenReturn(3L);

        EventDeletionConflictException exception =
                assertThrows(
                        EventDeletionConflictException.class,
                        () -> eventService.deleteEvent(1L));

        assertEquals(
                "Event cannot be deleted while it has reservations or waitlist entries",
                exception.getMessage());

        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    void deleteEvent_shouldThrowWhenEventDoesNotExist() {

        when(eventRepository.findById(999L))
                .thenReturn(Optional.empty());

        EventNotFoundException exception =
                assertThrows(
                        EventNotFoundException.class,
                        () -> eventService.deleteEvent(999L));

        assertEquals(
                "Event not found: 999",
                exception.getMessage());

        verify(eventRepository).findById(999L);
        verify(eventRepository, never())
                .delete(any(Event.class));
    }
}