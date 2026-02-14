package com.pm.pulseserver.modules.auth.app;

import com.pm.pulseserver.common.exception.NotFoundException;
import com.pm.pulseserver.modules.auth.api.dto.AuthResponse;
import com.pm.pulseserver.modules.auth.api.dto.LoginRequest;
import com.pm.pulseserver.modules.auth.api.dto.RegisterRequest;
import com.pm.pulseserver.modules.auth.domain.RefreshSession;
import com.pm.pulseserver.modules.auth.infra.AppAuthProperties;
import com.pm.pulseserver.modules.auth.infra.JwtProvider;
import com.pm.pulseserver.modules.auth.infra.RefreshSessionRepository;
import com.pm.pulseserver.modules.auth.infra.RefreshTokenUtil;
import com.pm.pulseserver.modules.users.domain.User;
import com.pm.pulseserver.modules.users.domain.UserProfile;
import com.pm.pulseserver.modules.users.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshSessionRepository refreshSessionRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtProvider jwtProvider;
    private final RefreshTokenUtil refreshTokenUtil;
    private final AppAuthProperties appAuthProperties;

    public record AuthResult(AuthResponse body, UUID sessionId, String rawRefreshToken) {}

    @Transactional
    public AuthResult register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalArgumentException("Username already taken");
        }

        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already taken");
        }

        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .username(req.username())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .build();

        UserProfile profile = UserProfile.builder()
                .user(user)
                .userId(userId)
                .displayName(req.displayName())
                .bio(null)
                .avatarUrl(null)
                .build();

        user.setProfile(profile);
        userRepository.save(user);

        return issueTokens(user);
    }

    @Transactional
    public AuthResult login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email()).orElseThrow(() -> new NotFoundException("Invalid Credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new NotFoundException("Invalid credentials");
        }

        return issueTokens(user);
    }

    @Transactional
    public AuthResult refresh(UUID sessionId, String rawRefreshToken) {
        Instant now = Instant.now();

        RefreshSession existing = refreshSessionRepository.findByIdAndRevokedAtIsNull(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        if (existing.isExpired(now)) {
            existing.revoke(now);
            throw new NotFoundException("Session expired");
        }

        String incomingHash = refreshTokenUtil.sha256(rawRefreshToken);
        if (!incomingHash.equals(existing.getRefreshTokenHash())) {
            existing.revoke(now);
            throw new NotFoundException("Invalid session");
        }

        existing.revoke(now);

        User user = existing.getUser();
        return issueTokens(user, existing.getId());
    }

    @Transactional
    public void logout(UUID sessionId) {
        Instant now = Instant.now();
        refreshSessionRepository.findById(sessionId).ifPresent(s -> {
            if (!s.isRevoked()) s.revoke(now);
        });
    }


    private AuthResult issueTokens(User user) {
        return issueTokens(user, null);
    }

    private AuthResult issueTokens(User user, UUID rotatedFromId) {
        Instant now = Instant.now();

        String rawRefresh = refreshTokenUtil.newRawToken();
        String refreshHash = refreshTokenUtil.sha256(rawRefresh);

        UUID sessionId = UUID.randomUUID();

        RefreshSession session = RefreshSession.builder()
                .id(sessionId)
                .user(user)
                .refreshTokenHash(refreshHash)
                .expiresAt(now.plus(appAuthProperties.refreshDays(), ChronoUnit.DAYS))
                .rotatedFromId(rotatedFromId)
                .build();

        refreshSessionRepository.save(session);

        String access = jwtProvider.createAccessToken(user.getId(), user.getUsername(), sessionId, now);
        return new AuthResult(new AuthResponse(access), sessionId, rawRefresh);
    }
}
