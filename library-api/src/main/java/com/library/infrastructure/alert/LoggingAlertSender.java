package com.library.infrastructure.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggingAlertSender implements AlertSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingAlertSender.class);

    @Override
    public void sendError(String title, String summary, Throwable error) {
        if (error != null) {
            log.error("[ALERT] {}: {}", title, summary, error);
        } else {
            log.error("[ALERT] {}: {}", title, summary);
        }
    }
}
