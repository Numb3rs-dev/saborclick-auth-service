package com.saborclick.auth.common.security.ratelimit;

import io.github.bucket4j.Bucket;

public interface RateLimiterManager {
    Bucket resolveBucket(String key);
}

