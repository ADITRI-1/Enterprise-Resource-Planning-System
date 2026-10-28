package com.iiitd.erp.io.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Query parameters accepted by every paged list endpoint.
 * Example: {@code ?q=cse&sort=title,desc&page=0&size=20}
 */
public record ListParams(
        @Schema(description = "Case-insensitive text search across all columns") String q,
        @Schema(description = "Column to sort by, optionally followed by ',desc'", example = "title,desc") String sort,
        @Schema(description = "Zero-based page number", defaultValue = "0") Integer page,
        @Schema(description = "Page size (max 200)", defaultValue = "20") Integer size) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 200;

    public static ListParams all() {
        return new ListParams(null, null, 0, MAX_SIZE);
    }
}
