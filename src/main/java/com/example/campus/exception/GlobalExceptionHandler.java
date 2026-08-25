package com.example.campus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidEventException.class)
    public ProblemDetail handleInvalidEvent(
            InvalidEventException exception) {

        return problem(
                HttpStatus.BAD_REQUEST,
                "Invalid Event",
                exception.getMessage()
        );
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ProblemDetail handleEventNotFound(
            EventNotFoundException exception) {

        return problem(
                HttpStatus.NOT_FOUND,
                "Event Not Found",
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidReservationException.class)
    public ProblemDetail handleInvalidReservation(
            InvalidReservationException exception) {

        return problem(
                HttpStatus.CONFLICT,
                "Invalid Reservation",
                exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(
            MethodArgumentNotValidException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed"
        );

        problem.setTitle("Validation Failed");

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        problem.setProperty("errors", errors);

        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMessageNotReadable(
            HttpMessageNotReadableException exception) {

        return problem(
                HttpStatus.BAD_REQUEST,
                "Invalid Request",
                "Request body is required or could not be parsed"
        );
    }

    @ExceptionHandler(EventDeletionConflictException.class)
    public ProblemDetail handleEventDeletionConflict(
            EventDeletionConflictException exception) {

        return problem(
                HttpStatus.CONFLICT,
                "Event Deletion Conflict",
                exception.getMessage()
        );
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ProblemDetail handleReservationNotFound(
            ReservationNotFoundException exception) {

        return problem(
                HttpStatus.NOT_FOUND,
                "Reservation Not Found",
                exception.getMessage()
        );
    }

    @ExceptionHandler(WaitlistEntryNotFoundException.class)
    public ProblemDetail handleWaitlistEntryNotFound(
            WaitlistEntryNotFoundException exception) {

        return problem(
                HttpStatus.NOT_FOUND,
                "Waitlist Entry Not Found",
                exception.getMessage()
        );
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ProblemDetail handleRateLimitExceeded(
            RateLimitExceededException exception) {

        return problem(
                HttpStatus.TOO_MANY_REQUESTS,
                "Too Many Requests",
                exception.getMessage()
        );
    }

    private ProblemDetail problem(
            HttpStatus status,
            String title,
            String detail) {

        ProblemDetail problem =
                ProblemDetail.forStatusAndDetail(status, detail);

        problem.setTitle(title);

        return problem;
    }
}