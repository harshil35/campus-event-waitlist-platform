package com.example.campus.controller;

import com.example.campus.model.Event;
import com.example.campus.repository.EventRepository;
import com.example.campus.repository.ReservationRepository;
import com.example.campus.repository.WaitlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        waitlistRepository.deleteAll();
        reservationRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @Test
    void createEvent_shouldReturn201AndPersistEvent() throws Exception {

        String requestBody = """
                {
                    "title": "Integration Test Event",
                    "description": "Testing the full request flow",
                    "startsAt": "2026-08-27T10:00:00",
                    "endsAt": "2026-08-27T12:00:00",
                    "venue": "Room 101",
                    "capacity": 50,
                    "organizerEmail": "test@example.com"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated());

        assertEquals(1, eventRepository.count());

        Event savedEvent = eventRepository.findAll().get(0);

        assertEquals(
                "Integration Test Event",
                savedEvent.getTitle());

        assertEquals(
                50,
                savedEvent.getCapacity());

        assertEquals(
                "test@example.com",
                savedEvent.getOrganizerEmail());
    }

    @Test
    void createEvent_shouldReturn400WhenDatesAreInvalid() throws Exception {

        String requestBody = """
            {
                "title": "Invalid Event",
                "description": "Invalid dates",
                "startsAt": "2026-08-27T12:00:00",
                "endsAt": "2026-08-27T10:00:00",
                "venue": "Room 101",
                "capacity": 50,
                "organizerEmail": "test@example.com"
            }
            """;

        mockMvc.perform(
                        post("/api/v1/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Invalid Event"))
                .andExpect(jsonPath("$.detail")
                        .value("endsAt must be after startsAt"));
    }

    @Test
    void getEventById_shouldReturn404WhenEventDoesNotExist() throws Exception {

        mockMvc.perform(
                        get("/api/v1/events/999999")
                )
                .andExpect(status().isNotFound());
    }
}