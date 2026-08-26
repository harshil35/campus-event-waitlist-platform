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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitlistServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private WaitlistRepository waitlistRepository;

    @InjectMocks
    private WaitlistService waitlistService;

    private Event event;
    private CreateWaitlistEntryRequest request;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(1L);
        event.setTitle("Spring Boot Workshop");
        event.setCapacity(50);

        request = new CreateWaitlistEntryRequest();
        request.setAttendeeEmail("student@example.com");
    }

    @Test
    void getWaitlist_shouldReturnEntriesWhenEventExists() {

        WaitlistEntry entry1 = new WaitlistEntry();
        entry1.setEvent(event);
        entry1.setAttendeeEmail("one@example.com");

        WaitlistEntry entry2 = new WaitlistEntry();
        entry2.setEvent(event);
        entry2.setAttendeeEmail("two@example.com");

        when(eventRepository.existsById(1L))
                .thenReturn(true);

        when(waitlistRepository
                .findByEventIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(entry1, entry2));

        List<WaitlistEntry> result =
                waitlistService.getWaitlist(1L);

        assertEquals(2, result.size());
        assertEquals(
                "one@example.com",
                result.get(0).getAttendeeEmail());
        assertEquals(
                "two@example.com",
                result.get(1).getAttendeeEmail());

        verify(eventRepository).existsById(1L);
        verify(waitlistRepository)
                .findByEventIdOrderByCreatedAtAsc(1L);
    }

    @Test
    void getWaitlist_shouldThrowWhenEventDoesNotExist() {

        when(eventRepository.existsById(999L))
                .thenReturn(false);

        EventNotFoundException exception =
                assertThrows(
                        EventNotFoundException.class,
                        () -> waitlistService.getWaitlist(999L));

        assertEquals(
                "Event not found: 999",
                exception.getMessage());

        verify(waitlistRepository, never())
                .findByEventIdOrderByCreatedAtAsc(anyLong());
    }

    @Test
    void getWaitlistEntry_shouldReturnEntryWhenFoundForEvent() {

        WaitlistEntry entry = new WaitlistEntry();
        entry.setEvent(event);
        entry.setAttendeeEmail("student@example.com");

        when(waitlistRepository.findById(10L))
                .thenReturn(Optional.of(entry));

        WaitlistEntry result =
                waitlistService.getWaitlistEntry(1L, 10L);

        assertNotNull(result);
        assertEquals(
                "student@example.com",
                result.getAttendeeEmail());

        verify(waitlistRepository).findById(10L);
    }

    @Test
    void getWaitlistEntry_shouldThrowWhenEntryDoesNotExist() {

        when(waitlistRepository.findById(999L))
                .thenReturn(Optional.empty());

        WaitlistEntryNotFoundException exception =
                assertThrows(
                        WaitlistEntryNotFoundException.class,
                        () -> waitlistService.getWaitlistEntry(
                                1L,
                                999L));

        assertEquals(
                "Waitlist entry not found: 999",
                exception.getMessage());

        verify(waitlistRepository).findById(999L);
    }

    @Test
    void getWaitlistEntry_shouldThrowWhenEntryBelongsToDifferentEvent() {

        Event anotherEvent = new Event();
        anotherEvent.setId(2L);

        WaitlistEntry entry = new WaitlistEntry();
        entry.setEvent(anotherEvent);
        entry.setAttendeeEmail("student@example.com");

        when(waitlistRepository.findById(10L))
                .thenReturn(Optional.of(entry));

        WaitlistEntryNotFoundException exception =
                assertThrows(
                        WaitlistEntryNotFoundException.class,
                        () -> waitlistService.getWaitlistEntry(
                                1L,
                                10L));

        assertEquals(
                "Waitlist entry does not belong to event: 1",
                exception.getMessage());

        verify(waitlistRepository).findById(10L);
    }

    @Test
    void removeFromWaitlist_shouldDeleteEntry() {

        WaitlistEntry entry = new WaitlistEntry();
        entry.setEvent(event);
        entry.setAttendeeEmail("student@example.com");

        when(waitlistRepository.findById(10L))
                .thenReturn(Optional.of(entry));

        waitlistService.removeFromWaitlist(1L, 10L);

        verify(waitlistRepository).findById(10L);
        verify(waitlistRepository).delete(entry);
    }

    @Test
    void removeFromWaitlist_shouldThrowWhenEntryDoesNotExist() {

        when(waitlistRepository.findById(999L))
                .thenReturn(Optional.empty());

        WaitlistEntryNotFoundException exception =
                assertThrows(
                        WaitlistEntryNotFoundException.class,
                        () -> waitlistService.removeFromWaitlist(
                                1L,
                                999L));

        assertEquals(
                "Waitlist entry not found: 999",
                exception.getMessage());

        verify(waitlistRepository, never())
                .delete(any(WaitlistEntry.class));
    }

    @Test
    void removeFromWaitlist_shouldThrowWhenEntryBelongsToDifferentEvent() {

        Event anotherEvent = new Event();
        anotherEvent.setId(2L);

        WaitlistEntry entry = new WaitlistEntry();
        entry.setEvent(anotherEvent);

        when(waitlistRepository.findById(10L))
                .thenReturn(Optional.of(entry));

        WaitlistEntryNotFoundException exception =
                assertThrows(
                        WaitlistEntryNotFoundException.class,
                        () -> waitlistService.removeFromWaitlist(
                                1L,
                                10L));

        assertEquals(
                "Waitlist entry does not belong to event: 1",
                exception.getMessage());

        verify(waitlistRepository, never())
                .delete(any(WaitlistEntry.class));
    }

    @Test
    void joinWaitlist_shouldCreateEntryWhenEventIsFull() {

        when(eventRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(event));

        when(reservationRepository.countByEventId(1L))
                .thenReturn(50L);

        when(reservationRepository
                .existsByEventIdAndAttendeeEmail(
                        1L,
                        "student@example.com"))
                .thenReturn(false);

        when(waitlistRepository
                .existsByEventIdAndAttendeeEmail(
                        1L,
                        "student@example.com"))
                .thenReturn(false);

        WaitlistEntry savedEntry = new WaitlistEntry();
        savedEntry.setEvent(event);
        savedEntry.setAttendeeEmail(
                "student@example.com");

        when(waitlistRepository.save(any(WaitlistEntry.class)))
                .thenReturn(savedEntry);

        WaitlistEntry result =
                waitlistService.joinWaitlist(
                        1L,
                        request);

        assertNotNull(result);
        assertEquals(
                event,
                result.getEvent());
        assertEquals(
                "student@example.com",
                result.getAttendeeEmail());

        verify(eventRepository)
                .findByIdForUpdate(1L);

        verify(reservationRepository)
                .countByEventId(1L);

        verify(reservationRepository)
                .existsByEventIdAndAttendeeEmail(
                        1L,
                        "student@example.com");

        verify(waitlistRepository)
                .existsByEventIdAndAttendeeEmail(
                        1L,
                        "student@example.com");

        verify(waitlistRepository)
                .save(any(WaitlistEntry.class));
    }

    @Test
    void joinWaitlist_shouldThrowWhenEventDoesNotExist() {

        when(eventRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.empty());

        EventNotFoundException exception =
                assertThrows(
                        EventNotFoundException.class,
                        () -> waitlistService.joinWaitlist(
                                999L,
                                request));

        assertEquals(
                "Event not found: 999",
                exception.getMessage());

        verify(waitlistRepository, never())
                .save(any(WaitlistEntry.class));
    }

    @Test
    void joinWaitlist_shouldRejectWhenSeatsAreAvailable() {

        when(eventRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(event));

        when(reservationRepository.countByEventId(1L))
                .thenReturn(49L);

        InvalidReservationException exception =
                assertThrows(
                        InvalidReservationException.class,
                        () -> waitlistService.joinWaitlist(
                                1L,
                                request));

        assertEquals(
                "Event still has available seats",
                exception.getMessage());

        verify(reservationRepository, never())
                .existsByEventIdAndAttendeeEmail(
                        anyLong(),
                        anyString());

        verify(waitlistRepository, never())
                .save(any(WaitlistEntry.class));
    }

    @Test
    void joinWaitlist_shouldRejectWhenAttendeeAlreadyReserved() {

        when(eventRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(event));

        when(reservationRepository.countByEventId(1L))
                .thenReturn(50L);

        when(reservationRepository
                .existsByEventIdAndAttendeeEmail(
                        1L,
                        "student@example.com"))
                .thenReturn(true);

        InvalidReservationException exception =
                assertThrows(
                        InvalidReservationException.class,
                        () -> waitlistService.joinWaitlist(
                                1L,
                                request));

        assertEquals(
                "Attendee already has a reservation",
                exception.getMessage());

        verify(waitlistRepository, never())
                .existsByEventIdAndAttendeeEmail(
                        anyLong(),
                        anyString());

        verify(waitlistRepository, never())
                .save(any(WaitlistEntry.class));
    }

    @Test
    void joinWaitlist_shouldRejectWhenAttendeeAlreadyWaitlisted() {

        when(eventRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(event));

        when(reservationRepository.countByEventId(1L))
                .thenReturn(50L);

        when(reservationRepository
                .existsByEventIdAndAttendeeEmail(
                        1L,
                        "student@example.com"))
                .thenReturn(false);

        when(waitlistRepository
                .existsByEventIdAndAttendeeEmail(
                        1L,
                        "student@example.com"))
                .thenReturn(true);

        InvalidReservationException exception =
                assertThrows(
                        InvalidReservationException.class,
                        () -> waitlistService.joinWaitlist(
                                1L,
                                request));

        assertEquals(
                "Attendee is already on the waitlist",
                exception.getMessage());

        verify(waitlistRepository, never())
                .save(any(WaitlistEntry.class));
    }
}