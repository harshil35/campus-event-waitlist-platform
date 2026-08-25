package com.example.campus.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String attendeeEmail) {

        String key = "rate:reservation:" + attendeeEmail;

        Long count = redisTemplate
                .opsForValue()
                .increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, WINDOW);
        }

        return count != null && count <= MAX_REQUESTS;
    }
}