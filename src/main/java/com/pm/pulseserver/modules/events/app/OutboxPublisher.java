package com.pm.pulseserver.modules.events.app;

import com.pm.pulseserver.modules.events.domain.OutboxEvent;
import com.pm.pulseserver.modules.events.domain.OutboxStatus;
import com.pm.pulseserver.modules.events.infra.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final EventDispatcher dispatcher;

    @Scheduled(fixedDelayString = "1000")
    @Transactional
    public void tick() {
        List<OutboxEvent> batch = outboxRepository.findReady("PENDING", Instant.now(), 50);

        for (OutboxEvent e : batch) {
            try {
                e.setStatus(OutboxStatus.PROCESSING);
                outboxRepository.save(e);

                dispatcher.dispatch(e);

                e.setStatus(OutboxStatus.DONE);
                e.setLastError(null);
                outboxRepository.save(e);
            } catch (Exception ex) {
                int attempts = e.getAttempts() + 1;
                e.setAttempts(attempts);
                e.setStatus(attempts >= 10 ? OutboxStatus.FAILED : OutboxStatus.PENDING);
                e.setLastError(ex.getMessage());

                long backoffSeconds = Math.min(60, (long) Math.pow(2, Math.min(attempts, 6)));
                e.setAvailableAt(Instant.now().plusSeconds(backoffSeconds));

                outboxRepository.save(e);
            }
        }
    }
}