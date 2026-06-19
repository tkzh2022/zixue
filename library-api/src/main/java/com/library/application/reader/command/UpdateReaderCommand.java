package com.library.application.reader.command;

public record UpdateReaderCommand(
        Long id,
        String name,
        String phone,
        String email
) {
}
