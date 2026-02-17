package com.pm.pulseserver.modules.events.app;

import com.pm.pulseserver.modules.events.domain.OutboxEvent;

public interface EventDispatcher {

    void dispatch(OutboxEvent event);
}
