package com.finorchestrator.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimiterService {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private final StringRedisTemplate redisTemplate;
    private final int requestsPerMinute;
    private final ConcurrentHashMap<String, RateBucket> localCache = new ConcurrentHashMap<>();

    public RateLimiterService(
            @Autowired(required = false) StringRedisTemplate redisTemplate,
            @Value("${finorchestrator.gateway.rate-limit.requests-per-minute:120}") int requestsPerMinute) {
        this.redisTemplate = redisTemplate;
        this.requestsPerMinute = requestsPerMinute;
    }

    public boolean isAllowed(String key) {
        if (redisTemplate != null) {
            try {
                String redisKey = "rate_limit:" + key;
                Long count = redisTemplate.opsForValue().increment(redisKey);
                if (count != null && count == 1) {
                    redisTemplate.expire(redisKey, 60, TimeUnit.SECONDS);
                }
                return count != null && count <= requestsPerMinute;
            } catch (Exception e) {
                log.warn("Redis unavailable for rate limiting, falling back to in-memory: {}", e.getMessage());
            }
        }

        // In-memory fallback
        long currentWindow = System.currentTimeMillis() / 60000;
        RateBucket bucket = localCache.compute(key, (k, existing) -> {
            if (existing == null || existing.window != currentWindow) {
                return new RateBucket(currentWindow, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        return bucket.count.get() <= requestsPerMinute;
    }

    private static class RateBucket {
        final long window;
        final AtomicInteger count;

        RateBucket(long window, AtomicInteger count) {
            this.window = window;
            this.count = count;
        }
    }
}
