package com.pm.pulseserver.modules.users.api;

import com.pm.pulseserver.modules.users.api.dto.PublicUserProfileResponse;
import com.pm.pulseserver.modules.users.app.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{username}")
    public PublicUserProfileResponse getPublicProfile(@PathVariable String username) {
        return userService.getPublicUserProfile(username);
    }
}
