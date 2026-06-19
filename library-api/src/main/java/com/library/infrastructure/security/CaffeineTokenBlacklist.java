package com.library.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@ConditionalOnProperty(name = "library.token-blacklist.type", havingValue = "caffeine", matchIfMissing = true)
public class CaffeineTokenBlacklist implements TokenBlacklist {

    private final Cache<String, Boolean> blacklistCache;

    public CaffeineTokenBlacklist() {
        this.blacklistCache = Caffeine.newBuilder()
                .expireAfter(new Expiry<String, Boolean>() {
                    @Override
                    public long expireAfterCreate(String key, Boolean value, long currentTime) {
                        return Long.MAX_VALUE; // Managed by put with duration
                    }

                    @Override
                    public long expireAfterUpdate(String key, Boolean value, long currentTime, @NonNegative long currentDuration) {
                        return currentDuration;
                    }

                    @Override
                    public long expireAfterRead(String key, Boolean value, long currentTime, @NonNegative long currentDuration) {
                        return currentDuration;
                    }
                })
                .build();
    }

    @Override
    public void add(String jti, Duration ttl) {
        if (ttl != null && !ttl.isNegative() && !ttl.isZero()) {
            blacklistCache.policy().expireVariably().ifPresent(policy -> 
                policy.put(jti, Boolean.TRUE, ttl)
            );
        }
    }

    @Override
    public boolean contains(String jti) {
        return blacklistCache.getIfPresent(jti) != null;
    }
}
