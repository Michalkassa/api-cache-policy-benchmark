package com.apicachebenchmark.timeline;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import com.apicachebenchmark.tweet.Tweet;

@Repository
public class TimelineRepository {

    private final JdbcClient jdbc;

    public TimelineRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Newest top-level tweets from the users {@code userId} follows. */
    public List<Tweet> findTimeline(long userId, int limit) {
        return jdbc.sql("""
                        SELECT t.id, t.author_id, t.reply_to_id, t.content, t.like_count, t.created_at
                        FROM follows f
                        JOIN tweets t ON t.author_id = f.followee_id AND t.reply_to_id IS NULL
                        WHERE f.follower_id = :userId
                        ORDER BY t.created_at DESC, t.id DESC
                        LIMIT :limit""")
                .param("userId", userId)
                .param("limit", limit)
                .query(Tweet.class)
                .list();
    }
}
