package com.library.application.auth.result;

import com.library.domain.user.UserAccount;

public record AuthResult(
        String accessToken,
        String refreshToken,
        long accessExpiresIn,
        long refreshExpiresIn,
        UserAccount userAccount
) {
}
