package com.apicachebenchmark.tweet;

import java.time.OffsetDateTime;

/** A tweet, or a reply when {@code replyToId} is set. */
public record Tweet(long id, long authorId, Long replyToId, String content, int likeCount, OffsetDateTime createdAt) {
}
