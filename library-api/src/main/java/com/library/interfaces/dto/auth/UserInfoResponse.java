package com.library.interfaces.dto.auth;

public record UserInfoResponse(
        Long id,
        String username,
        String role,
        String status
) {
}
