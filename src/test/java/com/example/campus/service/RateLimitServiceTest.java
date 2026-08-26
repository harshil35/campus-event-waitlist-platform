package com.example.campus.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RateLimitService rateLimitService;

    @Test
    void isAllowed_shouldAllowFirstRequest() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment(
                "rate:reservation:test@example.com"))
                .thenReturn(1L);

        boolean result =
                rateLimitService.isAllowed("test@example.com");

        assertTrue(result);

        verify(valueOperations)
                .increment("rate:reservation:test@example.com");

        verify(redisTemplate)
                .expire(
                        "rate:reservation:test@example.com",
                        Duration.ofSeconds(60));
    }

    @Test
    void isAllowed_shouldAllowRequestAtLimit() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment(
                "rate:reservation:test@example.com"))
                .thenReturn(10L);

        boolean result =
                rateLimitService.isAllowed("test@example.com");

        assertTrue(result);

        verify(valueOperations)
                .increment("rate:reservation:test@example.com");

        verify(redisTemplate, never())
                .expire(
                        anyString(),
                        any(Duration.class));
    }

    @Test
    void isAllowed_shouldRejectRequestAboveLimit() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment(
                "rate:reservation:test@example.com"))
                .thenReturn(11L);

        boolean result =
                rateLimitService.isAllowed("test@example.com");

        assertFalse(result);

        verify(valueOperations)
                .increment("rate:reservation:test@example.com");
    }

    @Test
    void isAllowed_shouldSetTtlOnlyOnFirstRequest() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment(
                "rate:reservation:test@example.com"))
                .thenReturn(1L);

        rateLimitService.isAllowed("test@example.com");

        verify(redisTemplate)
                .expire(
                        "rate:reservation:test@example.com",
                        Duration.ofSeconds(60));

        reset(redisTemplate, valueOperations);

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment(
                "rate:reservation:test@example.com"))
                .thenReturn(2L);

        rateLimitService.isAllowed("test@example.com");

        verify(redisTemplate, never())
                .expire(
                        anyString(),
                        any(Duration.class));
    }

    @Test
    void isAllowed_shouldKeepUsersInSeparateRateLimitBuckets() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment(
                "rate:reservation:user1@example.com"))
                .thenReturn(1L);

        when(valueOperations.increment(
                "rate:reservation:user2@example.com"))
                .thenReturn(1L);

        assertTrue(
                rateLimitService.isAllowed(
                        "user1@example.com"));

        assertTrue(
                rateLimitService.isAllowed(
                        "user2@example.com"));

        verify(valueOperations)
                .increment(
                        "rate:reservation:user1@example.com");

        verify(valueOperations)
                .increment(
                        "rate:reservation:user2@example.com");
    }

    @Test
    void isAllowed_shouldRejectWhenRedisReturnsNull() {

        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        when(valueOperations.increment(
                "rate:reservation:test@example.com"))
                .thenReturn(null);

        boolean result =
                rateLimitService.isAllowed("test@example.com");

        assertFalse(result);

        verify(valueOperations)
                .increment("rate:reservation:test@example.com");

        verify(redisTemplate, never())
                .expire(anyString(), any(Duration.class));
    }
}