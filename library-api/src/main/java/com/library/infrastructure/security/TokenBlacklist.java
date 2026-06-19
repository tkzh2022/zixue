package com.library.infrastructure.security;

import java.time.Duration;

public interface TokenBlacklist {

    void add(String jti, Duration ttl);

    boolean contains(String jti);
}
