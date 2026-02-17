package com.pm.pulseserver.modules.events.domain;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    DONE,
    FAILED
}
