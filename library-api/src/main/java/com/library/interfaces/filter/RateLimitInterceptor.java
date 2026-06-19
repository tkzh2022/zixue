package com.library.interfaces.filter;

import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import com.library.domain.user.UserAccount;
import com.library.infrastructure.ratelimit.RateLimiter;
import com.library.interfaces.annotation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter;
    private final boolean enabled;

    public RateLimitInterceptor(RateLimiter rateLimiter,
                              @Value("${library.rate-limit.enabled:true}") boolean enabled) {
        this.rateLimiter = rateLimiter;
        this.enabled = enabled;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!enabled || !(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (rateLimit == null) {
            return true;
        }

        String key = buildKey(request, rateLimit.key());
        if (!rateLimiter.tryConsume(key, rateLimit.limit(), rateLimit.periodInSeconds())) {
            response.setHeader("Retry-After", String.valueOf(rateLimit.periodInSeconds()));
            throw new BusinessException(ResultCode.RATE_LIMITED);
        }

        return true;
    }

    private String buildKey(HttpServletRequest request, String keyType) {
        String ip = getClientIp(request);
        String user = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserAccount) {
            user = ((UserAccount) auth.getPrincipal()).username();
        }

        switch (keyType) {
            case "user":
                return "rate:user:" + user;
            case "ip+user":
                return "rate:ip+user:" + ip + ":" + user;
            case "ip":
            default:
                return "rate:ip:" + ip;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
