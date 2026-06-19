package com.library.application.auth.command;

public record LoginCommand(String username, String password, String ip, String userAgent) {
}
