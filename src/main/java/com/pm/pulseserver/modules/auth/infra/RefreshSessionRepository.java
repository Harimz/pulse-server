package com.pm.pulseserver.modules.auth.infra;

import com.pm.pulseserver.modules.auth.domain.RefreshSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionRepository extends JpaRepository<RefreshSession, UUID> {

    Optional<RefreshSession> findByIdAndRevokedAtIsNull(UUID id);
}
