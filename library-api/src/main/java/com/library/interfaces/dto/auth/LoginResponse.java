package com.library.interfaces.dto.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long accessExpiresIn,
        long refreshExpiresIn,
        UserInfoResponse user
) {
}
