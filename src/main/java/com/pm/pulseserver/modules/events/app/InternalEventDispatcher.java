package com.pm.pulseserver.modules.events.app;

import com.pm.pulseserver.modules.events.domain.EventTypes;
import com.pm.pulseserver.modules.events.domain.OutboxEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InternalEventDispatcher implements EventDispatcher {

    private final UserFollowedHandler userFollowedHandler;
    private final UserMentionedHandler userMentionedHandler;

    @Override
    public void dispatch(OutboxEvent event) {
        if (EventTypes.USER_FOLLOWED.equals(event.getEventType())) {
            userFollowedHandler.handle(event);
            return;
        }
        if (EventTypes.USER_MENTIONED.equals(event.getEventType())) {
            userMentionedHandler.handle(event);
        }
    }
}
