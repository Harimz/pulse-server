package com.pm.pulseserver.modules.comment.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(@NotBlank String body) {}
