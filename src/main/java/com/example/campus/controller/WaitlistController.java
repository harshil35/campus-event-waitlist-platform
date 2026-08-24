package com.example.campus.controller;

import com.example.campus.dto.CreateWaitlistEntryRequest;
import com.example.campus.model.WaitlistEntry;
import com.example.campus.service.WaitlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/events/{eventId}/waitlist")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @GetMapping
    public ResponseEntity<List<WaitlistEntry>> getWaitlist(
            @PathVariable Long eventId) {

        return ResponseEntity.ok(
                waitlistService.getWaitlist(eventId));
    }

    @GetMapping("/{entryId}")
    public ResponseEntity<WaitlistEntry> getWaitlistEntry(
            @PathVariable Long eventId,
            @PathVariable Long entryId) {

        return ResponseEntity.ok(
                waitlistService.getWaitlistEntry(
                        eventId, entryId));
    }

    @PostMapping
    public ResponseEntity<WaitlistEntry> joinWaitlist(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateWaitlistEntryRequest request) {

        WaitlistEntry entry =
                waitlistService.joinWaitlist(eventId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(entry);
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> removeFromWaitlist(
            @PathVariable Long eventId,
            @PathVariable Long entryId) {

        waitlistService.removeFromWaitlist(eventId, entryId);

        return ResponseEntity.noContent().build();
    }
}