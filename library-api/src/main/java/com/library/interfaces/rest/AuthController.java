package com.library.interfaces.rest;

import com.library.application.auth.AuthApplicationService;
import com.library.application.auth.command.LoginCommand;
import com.library.application.auth.command.RefreshCommand;
import com.library.application.auth.command.RegisterCommand;
import com.library.application.auth.result.AuthResult;
import com.library.domain.shared.Result;
import com.library.interfaces.dto.auth.LoginRequest;
import com.library.interfaces.dto.auth.LoginResponse;
import com.library.interfaces.dto.auth.LogoutRequest;
import com.library.interfaces.dto.auth.RefreshRequest;
import com.library.interfaces.dto.auth.RegisterRequest;
import com.library.interfaces.dto.auth.UserInfoResponse;
import com.library.interfaces.annotation.RateLimit;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.library.domain.user.UserAccount;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/login")
    @RateLimit(key = "ip+user", limit = 10, periodInSeconds = 60)
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginCommand cmd = new LoginCommand(
                request.getUsername(),
                request.getPassword(),
                getClientIp(httpRequest),
                httpRequest.getHeader("User-Agent")
        );
        AuthResult authResult = authApplicationService.login(cmd);
        return Result.ok(mapToLoginResponse(authResult));
    }

    @PostMapping("/register")
    @RateLimit(key = "ip", limit = 5, periodInSeconds = 60)
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        RegisterCommand cmd = new RegisterCommand(
                request.getUsername(),
                request.getPassword(),
                request.getName(),
                request.getPhone(),
                request.getEmail(),
                getClientIp(httpRequest),
                httpRequest.getHeader("User-Agent")
        );
        AuthResult authResult = authApplicationService.register(cmd);
        return Result.ok(mapToLoginResponse(authResult));
    }

    @PostMapping("/refresh")
    @RateLimit(key = "user", limit = 30, periodInSeconds = 60)
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest httpRequest) {
        RefreshCommand cmd = new RefreshCommand(
                request.getRefreshToken(),
                getClientIp(httpRequest),
                httpRequest.getHeader("User-Agent")
        );
        AuthResult authResult = authApplicationService.refresh(cmd);
        return Result.ok(mapToLoginResponse(authResult));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody(required = false) LogoutRequest request,
                               HttpServletRequest httpRequest) {
        authApplicationService.logoutTokens(
                extractBearerToken(httpRequest),
                request == null ? null : request.refreshToken());
        return Result.ok(null);
    }

    @PostMapping("/logout-all")
    public Result<Void> logoutAll(@AuthenticationPrincipal UserAccount account) {
        authApplicationService.logoutAll(account.id());
        return Result.ok(null);
    }

    @GetMapping("/me")
    public Result<UserInfoResponse> me(@AuthenticationPrincipal UserAccount account) {
        return Result.ok(new UserInfoResponse(
                account.id(), account.username(), account.role().name(), account.status().name()));
    }

    private LoginResponse mapToLoginResponse(AuthResult authResult) {
        UserInfoResponse userInfo = new UserInfoResponse(
                authResult.userAccount().id(),
                authResult.userAccount().username(),
                authResult.userAccount().role().name(),
                authResult.userAccount().status().name()
        );
        return new LoginResponse(
                authResult.accessToken(),
                authResult.refreshToken(),
                authResult.accessExpiresIn(),
                authResult.refreshExpiresIn(),
                userInfo
        );
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
    }
}
