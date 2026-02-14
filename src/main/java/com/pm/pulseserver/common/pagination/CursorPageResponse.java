package com.pm.pulseserver.common.pagination;

import java.util.List;

public record CursorPageResponse<T>(
        List<T> items,
        String nextCursor
) {
}
