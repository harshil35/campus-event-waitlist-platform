package com.example.campus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class UpdateEventRequest {

    @NotBlank(message = "title is required")
    private String title;

    private String description;

    @NotNull(message = "startsAt is required")
    private LocalDateTime startsAt;

    @NotNull(message = "endsAt is required")
    private LocalDateTime endsAt;

    @NotBlank(message = "venue is required")
    private String venue;

    @Min(value = 1, message = "capacity must be at least 1")
    private int capacity;

    @NotBlank(message = "organizerEmail is required")
    @Email(message = "organizerEmail must be a valid email")
    private String organizerEmail;

    public UpdateEventRequest() {
    }

    public @NotBlank(message = "title is required") String getTitle() {
        return title;
    }

    public void setTitle(@NotBlank(message = "title is required") String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public @NotNull(message = "startsAt is required") LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(@NotNull(message = "startsAt is required") LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public @NotNull(message = "endsAt is required") LocalDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(@NotNull(message = "endsAt is required") LocalDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public @NotBlank(message = "venue is required") String getVenue() {
        return venue;
    }

    public void setVenue(@NotBlank(message = "venue is required") String venue) {
        this.venue = venue;
    }

    @Min(value = 1, message = "capacity must be at least 1")
    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(@Min(value = 1, message = "capacity must be at least 1") int capacity) {
        this.capacity = capacity;
    }

    public @NotBlank(message = "organizerEmail is required") @Email(message = "organizerEmail must be a valid email") String getOrganizerEmail() {
        return organizerEmail;
    }

    public void setOrganizerEmail(@NotBlank(message = "organizerEmail is required") @Email(message = "organizerEmail must be a valid email") String organizerEmail) {
        this.organizerEmail = organizerEmail;
    }
}