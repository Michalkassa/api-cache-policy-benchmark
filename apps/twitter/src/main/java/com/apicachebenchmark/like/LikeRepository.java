package com.apicachebenchmark.like;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class LikeRepository {

    private final JdbcClient jdbc;

    public LikeRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Returns false if the user had already liked the tweet, so repeated likes are idempotent. */
    public boolean save(long userId, long tweetId) {
        return jdbc.sql("INSERT INTO likes (user_id, tweet_id) VALUES (:userId, :tweetId) ON CONFLICT DO NOTHING")
                .param("userId", userId)
                .param("tweetId", tweetId)
                .update() == 1;
    }

    public boolean exists(long userId, long tweetId) {
        return jdbc.sql("SELECT EXISTS (SELECT 1 FROM likes WHERE user_id = :userId AND tweet_id = :tweetId)")
                .param("userId", userId)
                .param("tweetId", tweetId)
                .query(Boolean.class)
                .single();
    }

    public long countByTweetId(long tweetId) {
        return jdbc.sql("SELECT count(*) FROM likes WHERE tweet_id = :tweetId")
                .param("tweetId", tweetId)
                .query(Long.class)
                .single();
    }
}
