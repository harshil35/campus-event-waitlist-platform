package com.example.campus.service;

import com.example.campus.dto.CreateReservationRequest;
import com.example.campus.exception.EventNotFoundException;
import com.example.campus.exception.InvalidReservationException;
import com.example.campus.exception.ReservationNotFoundException;
import com.example.campus.model.Event;
import com.example.campus.model.Reservation;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private WaitlistRepository waitlistRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Event event;
    private CreateReservationRequest request;

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(1L);
        event.setTitle("Spring Boot Workshop");
        event.setCapacity(50);

        request = new CreateReservationRequest();
        request.setAttendeeEmail("student@example.com");
    }

    @Test
    void getReservation_shouldReturnReservationWhenFoundForEvent() {

        Reservation reservation = new Reservation();
        reservation.setEvent(event);
        reservation.setAttendeeEmail("student@example.com");

        when(reservationRepository.findById(10L))
                .thenReturn(Optional.of(reservation));

        Reservation result =
                reservationService.getReservation(1L, 10L);

        assertNotNull(result);
        assertEquals(
                "student@example.com",
                result.getAttendeeEmail());

        verify(reservationRepository).findById(10L);
    }

    @Test
    void getReservation_shouldThrowWhenReservationDoesNotExist() {

        when(reservationRepository.findById(999L))
                .thenReturn(Optional.empty());

        ReservationNotFoundException exception =
                assertThrows(
                        ReservationNotFoundException.class,
                        () -> reservationService.getReservation(
                                1L,
                                999L));

        assertEquals(
                "Reservation not found: 999",
                exception.getMessage());

        verify(reservationRepository).findById(999L);
    }

    @Test
    void getReservation_shouldThrowWhenReservationBelongsToDifferentEvent() {

        Event anotherEvent = new Event();
        anotherEvent.setId(2L);

        Reservation reservation = new Reservation();
        reservation.setEvent(anotherEvent);

        when(reservationRepository.findById(10L))
                .thenReturn(Optional.of(reservation));

        ReservationNotFoundException exception =
                assertThrows(
                        ReservationNotFoundException.class,
                        () -> reservationService.getReservation(
                                1L,
                                10L));

        assertEquals(
                "Reservation does not belong to event: 1",
                exception.getMessage());

        verify(reservationRepository).findById(10L);
    }

    @Test
    void getReservationsForEvent_shouldReturnReservationsWhenEventExists() {

        Reservation reservation1 = new Reservation();
        reservation1.setEvent(event);
        reservation1.setAttendeeEmail("one@example.com");

        Reservation reservation2 = new Reservation();
        reservation2.setEvent(event);
        reservation2.setAttendeeEmail("two@example.com");

        when(eventRepository.existsById(1L))
                .thenReturn(true);

        when(reservationRepository.findByEventId(1L))
                .thenReturn(List.of(reservation1, reservation2));

        List<Reservation> result =
                reservationService.getReservationsForEvent(1L);

        assertEquals(2, result.size());

        assertEquals(
                "one@example.com",
                result.get(0).getAttendeeEmail());

        assertEquals(
                "two@example.com",
                result.get(1).getAttendeeEmail());

        verify(eventRepository).existsById(1L);
        verify(reservationRepository).findByEventId(1L);
    }

    @Test
    void getReservationsForEvent_shouldThrowWhenEventDoesNotExist() {

        when(eventRepository.existsById(999L))
                .thenReturn(false);

        EventNotFoundException exception =
                assertThrows(
                        EventNotFoundException.class,
                        () -> reservationService
                                .getReservationsForEvent(999L));

        assertEquals(
                "Event not found: 999",
                exception.getMessage());

        verify(eventRepository).existsById(999L);

        verify(reservationRepository, never())
                .findByEventId(anyLong());
    }

    @Test
    void createReservation_shouldCreateReservationWhenSeatAvailable() {

        when(eventRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(event));

        when(reservationRepository
                .existsByEventIdAndAttendeeEmail(
                        1L,
                        "student@example.com"))
                .thenReturn(false);

        when(reservationRepository.countByEventId(1L))
                .thenReturn(10L);

        Reservation savedReservation = new Reservation();
        savedReservation.setEvent(event);
        savedReservation.setAttendeeEmail(
                "student@example.com");

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(savedReservation);

        Reservation result =
                reservationService.createReservation(
                        1L,
                        request);

        assertNotNull(result);

        assertEquals(
                "student@example.com",
                result.getAttendeeEmail());

        assertEquals(
                event,
                result.getEvent());

        verify(eventRepository)
                .findByIdForUpdate(1L);

        verify(reservationRepository)
                .existsByEventIdAndAttendeeEmail(
                        1L,
                        "student@example.com");

        verify(reservationRepository)
                .countByEventId(1L);

        verify(reservationRepository)
                .save(any(Reservation.class));
    }

    @Test
    void createReservation_shouldThrowWhenEventDoesNotExist() {

        when(eventRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.empty());

        EventNotFoundException exception =
                assertThrows(
                        EventNotFoundException.class,
                        () -> reservationService.createReservation(
                                999L,
                                request));

        assertEquals(
                "Event not found: 999",
                exception.getMessage());

        verify(eventRepository)
                .findByIdForUpdate(999L);

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void createReservation_shouldRejectDuplicateReservation() {

        when(eventRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(event));

        when(reservationRepository
                .existsByEventIdAndAttendeeEmail(
                        1L,
                        "student@example.com"))
                .thenReturn(true);

        InvalidReservationException exception =
                assertThrows(
                        InvalidReservationException.class,
                        () -> reservationService
                                .createReservation(
                                        1L,
                                        request));

        assertEquals(
                "Attendee has already reserved this event",
                exception.getMessage());

        verify(reservationRepository, never())
                .countByEventId(anyLong());

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void createReservation_shouldRejectWhenEventIsFull() {

        when(eventRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(event));

        when(reservationRepository
                .existsByEventIdAndAttendeeEmail(
                        1L,
                        "student@example.com"))
                .thenReturn(false);

        when(reservationRepository.countByEventId(1L))
                .thenReturn(50L);

        InvalidReservationException exception =
                assertThrows(
                        InvalidReservationException.class,
                        () -> reservationService
                                .createReservation(
                                        1L,
                                        request));

        assertEquals(
                "Event is full",
                exception.getMessage());

        verify(reservationRepository, never())
                .save(any(Reservation.class));
    }

    @Test
    void cancelReservation_shouldDeleteReservationWhenNoWaitlist() {

        Reservation reservation = new Reservation();
        reservation.setEvent(event);
        reservation.setAttendeeEmail(
                "student@example.com");

        when(eventRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(event));

        when(reservationRepository.findById(10L))
                .thenReturn(Optional.of(reservation));

        when(waitlistRepository
                .findFirstByEventIdOrderByCreatedAtAsc(1L))
                .thenReturn(Optional.empty());

        reservationService.cancelReservation(1L, 10L);

        verify(reservationRepository)
                .delete(reservation);

        verify(waitlistRepository)
                .findFirstByEventIdOrderByCreatedAtAsc(1L);

        verify(reservationRepository, never())
                .save(any(Reservation.class));

        verify(waitlistRepository, never())
                .delete(any(WaitlistEntry.class));
    }

    @Test
    void cancelReservation_shouldPromoteFirstWaitlistedAttendee() {

        Reservation reservation = new Reservation();
        reservation.setEvent(event);
        reservation.setAttendeeEmail(
                "current@example.com");

        WaitlistEntry waitlistEntry = new WaitlistEntry();
        waitlistEntry.setEvent(event);
        waitlistEntry.setAttendeeEmail(
                "waiting@example.com");

        when(eventRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(event));

        when(reservationRepository.findById(10L))
                .thenReturn(Optional.of(reservation));

        when(waitlistRepository
                .findFirstByEventIdOrderByCreatedAtAsc(1L))
                .thenReturn(Optional.of(waitlistEntry));

        reservationService.cancelReservation(1L, 10L);

        verify(reservationRepository)
                .delete(reservation);

        verify(reservationRepository)
                .save(argThat(saved ->
                        saved.getEvent().equals(event)
                                && saved.getAttendeeEmail()
                                .equals("waiting@example.com")
                ));

        verify(waitlistRepository)
                .delete(waitlistEntry);
    }

    @Test
    void cancelReservation_shouldThrowWhenEventDoesNotExist() {

        when(eventRepository.findByIdForUpdate(999L))
                .thenReturn(Optional.empty());

        EventNotFoundException exception =
                assertThrows(
                        EventNotFoundException.class,
                        () -> reservationService
                                .cancelReservation(999L, 10L));

        assertEquals(
                "Event not found: 999",
                exception.getMessage());

        verify(reservationRepository, never())
                .delete(any(Reservation.class));
    }

    @Test
    void cancelReservation_shouldThrowWhenReservationDoesNotExist() {

        when(eventRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(event));

        when(reservationRepository.findById(999L))
                .thenReturn(Optional.empty());

        ReservationNotFoundException exception =
                assertThrows(
                        ReservationNotFoundException.class,
                        () -> reservationService
                                .cancelReservation(1L, 999L));

        assertEquals(
                "Reservation not found: 999",
                exception.getMessage());

        verify(reservationRepository, never())
                .delete(any(Reservation.class));
    }

    @Test
    void cancelReservation_shouldThrowWhenReservationBelongsToDifferentEvent() {

        Event anotherEvent = new Event();
        anotherEvent.setId(2L);

        Reservation reservation = new Reservation();
        reservation.setEvent(anotherEvent);

        when(eventRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(event));

        when(reservationRepository.findById(10L))
                .thenReturn(Optional.of(reservation));

        ReservationNotFoundException exception =
                assertThrows(
                        ReservationNotFoundException.class,
                        () -> reservationService
                                .cancelReservation(1L, 10L));

        assertEquals(
                "Reservation does not belong to event: 1",
                exception.getMessage());

        verify(reservationRepository, never())
                .delete(any(Reservation.class));
    }
}