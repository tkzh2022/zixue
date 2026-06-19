package com.library.application.auth.command;

public record RefreshCommand(String refreshToken, String ip, String userAgent) {
}
