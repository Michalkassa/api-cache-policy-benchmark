package com.apicachebenchmark.document;

import java.time.OffsetDateTime;

/** A document. {@code version} increases on every edit; stats computed from an older version are stale. */
public record Document(long id, String title, String content, long version, OffsetDateTime updatedAt) {
}
