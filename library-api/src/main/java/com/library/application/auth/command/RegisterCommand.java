package com.library.application.auth.command;

public record RegisterCommand(String username, String password, String name, String phone, String email, String ip, String userAgent) {
}
