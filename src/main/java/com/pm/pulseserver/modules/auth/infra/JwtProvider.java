package com.pm.pulseserver.modules.auth.infra;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    private final AppAuthProperties props;
    private final Key key;

    public JwtProvider(AppAuthProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.jwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(UUID userId, String username, UUID sessionId, Instant now) {
        Instant exp = now.plusSeconds(props.accessMinutes() * 60L);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("sid", sessionId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims parse(String jwt) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }
}
