package com.pm.pulseserver.modules.posts.app;

import com.pm.pulseserver.modules.users.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MentionService {

    private static final Pattern MENTION = Pattern.compile("(?<!\\w)@([a-zA-Z0-9_\\.]{3,30})");

    private final UserRepository userRepository;

    public MentionResult extractMentionedUserIds(String body) {
        Set<String> usernames = new LinkedHashSet<>();
        Matcher m = MENTION.matcher(body);
        while (m.find()) usernames.add(m.group(1));

        if (usernames.isEmpty()) {
            return new MentionResult(List.of(), List.of());
        }

        var users = userRepository.findAllByUsernameIn(usernames);
        var ids = users.stream().map(u -> u.getId()).toList();

        var validUsernames = users.stream().map(u -> u.getUsername()).toList();

        return new MentionResult(ids, validUsernames);
    }

    public record MentionResult(List<UUID> userIds, List<String> usernames) {}
}
