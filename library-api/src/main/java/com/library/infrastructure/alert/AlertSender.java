package com.library.infrastructure.alert;

public interface AlertSender {
    void sendError(String title, String summary, Throwable error);
}
