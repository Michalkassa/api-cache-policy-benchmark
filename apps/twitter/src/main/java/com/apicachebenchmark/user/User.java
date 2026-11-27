package com.apicachebenchmark.user;

import java.time.OffsetDateTime;

public record User(long id, String username, String displayName, OffsetDateTime createdAt) {
}
