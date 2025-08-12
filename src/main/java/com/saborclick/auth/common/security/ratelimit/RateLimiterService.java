package com.saborclick.auth.common.security.ratelimit;

import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RateLimiterManager rateLimiterManager;

    public boolean isAllowed(HttpServletRequest request) {
        String key = extractClientKey(request);
        Bucket bucket = rateLimiterManager.resolveBucket(key);
        boolean allowed = bucket.tryConsume(1);

        if (!allowed) {
            log.warn("❌ Rate limit excedido para {}", key);
        }

        return allowed;
    }

    private String extractClientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isEmpty())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}
