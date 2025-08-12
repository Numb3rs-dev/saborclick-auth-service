package com.saborclick.auth.common.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redis;

    public void revokeToken(String token, long expirationMillis) {
        redis.opsForValue().set(buildKey(token), "revoked", Duration.ofMillis(expirationMillis));
    }

    public boolean isTokenRevoked(String token) {
        return redis.hasKey(buildKey(token));
    }

    private String buildKey(String token) {
        return "revoked:token:" + token;
    }
}

