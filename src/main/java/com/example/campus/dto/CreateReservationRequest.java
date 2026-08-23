package com.example.campus.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateReservationRequest {

    @NotBlank(message = "attendeeEmail is required")
    @Email(message = "attendeeEmail must be a valid email")
    private String attendeeEmail;

    public CreateReservationRequest() {
    }

    public @NotBlank(message = "attendeeEmail is required") @Email(message = "attendeeEmail must be a valid email") String getAttendeeEmail() {
        return attendeeEmail;
    }

    public void setAttendeeEmail(@NotBlank(message = "attendeeEmail is required") @Email(message = "attendeeEmail must be a valid email") String attendeeEmail) {
        this.attendeeEmail = attendeeEmail;
    }
}