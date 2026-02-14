package com.pm.pulseserver.modules.users.api;

import com.pm.pulseserver.modules.auth.infra.JwtAuthenticationFilter;
import com.pm.pulseserver.modules.users.api.dto.PublicUserProfileResponse;
import com.pm.pulseserver.modules.users.api.dto.UpdateMyProfileRequest;
import com.pm.pulseserver.modules.users.app.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{username}")
    public PublicUserProfileResponse getPublicProfile(@PathVariable String username) {
        return userService.getPublicUserProfile(username);
    }

    @GetMapping("/me/profile")
    public PublicUserProfileResponse getMyProfile(@AuthenticationPrincipal JwtAuthenticationFilter.AuthPrincipal principal) {
        UUID userId = UUID.fromString(principal.userId());

        return userService.getMyPublicUserProfile(userId);
    }

    @PatchMapping("/me/profile")
    public PublicUserProfileResponse updateMyProfile(
            @AuthenticationPrincipal JwtAuthenticationFilter.AuthPrincipal principal,
            @Valid @RequestBody UpdateMyProfileRequest req
    ) {
        UUID userId = UUID.fromString(principal.userId());
        return userService.updateMyProfile(userId, principal.username(), req);
    }
}
