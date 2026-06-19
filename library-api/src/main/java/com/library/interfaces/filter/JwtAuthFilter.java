package com.library.interfaces.filter;

import com.library.domain.user.UserAccount;
import com.library.domain.user.UserAccountRepository;
import com.library.infrastructure.security.JwtService;
import com.library.infrastructure.security.TokenBlacklist;
import com.library.domain.shared.exception.ResultCode;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenBlacklist tokenBlacklist;
    private final UserAccountRepository userAccountRepository;

    public JwtAuthFilter(JwtService jwtService, TokenBlacklist tokenBlacklist, UserAccountRepository userAccountRepository) {
        this.jwtService = jwtService;
        this.tokenBlacklist = tokenBlacklist;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            JwtService.ParsedJwt parsedJwt = jwtService.parse(token);

            if (tokenBlacklist.contains(parsedJwt.jti())) {
                request.setAttribute("jwtError", "Token is blacklisted");
                request.setAttribute("jwtErrorCode", ResultCode.AUTH_REFRESH_TOKEN_INVALID);
                filterChain.doFilter(request, response);
                return;
            }

            Optional<UserAccount> accountOpt = userAccountRepository.findById(parsedJwt.userId());
            if (accountOpt.isEmpty() || !accountOpt.get().isActive()) {
                request.setAttribute("jwtError", "User is disabled or not found");
                request.setAttribute("jwtErrorCode", ResultCode.AUTH_ACCOUNT_LOCKED);
                filterChain.doFilter(request, response);
                return;
            }

            UserAccount account = accountOpt.get();
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    account, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + account.role().name()))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("userId", account.id());

        } catch (Exception e) {
            request.setAttribute("jwtError", e.getMessage());
            request.setAttribute("jwtErrorCode",
                    causedByExpiredToken(e) ? ResultCode.AUTH_ACCESS_TOKEN_EXPIRED : ResultCode.AUTH_TOKEN_MISSING);
        }

        filterChain.doFilter(request, response);
    }

    private boolean causedByExpiredToken(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof ExpiredJwtException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
