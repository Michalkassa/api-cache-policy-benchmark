package com.apicachebenchmark.tweet;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class TweetRepository {

    private static final String COLUMNS = "id, author_id, reply_to_id, content, like_count, created_at";

    private final JdbcClient jdbc;

    public TweetRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Tweet save(long authorId, Long replyToId, String content) {
        return jdbc.sql("""
                        INSERT INTO tweets (author_id, reply_to_id, content)
                        VALUES (:authorId, :replyToId, :content)
                        RETURNING\s""" + COLUMNS)
                .param("authorId", authorId)
                .param("replyToId", replyToId)
                .param("content", content)
                .query(Tweet.class)
                .single();
    }

    public Optional<Tweet> findById(long id) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM tweets WHERE id = :id")
                .param("id", id)
                .query(Tweet.class)
                .optional();
    }

    public List<Tweet> findByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return jdbc.sql("SELECT " + COLUMNS + " FROM tweets WHERE id IN (:ids) ORDER BY id")
                .param("ids", ids)
                .query(Tweet.class)
                .list();
    }

    /** Tweets with {@code fromId <= id < toId}: loads one cache block ({@code block:{id / size}}). */
    public List<Tweet> findByIdRange(long fromId, long toId) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM tweets WHERE id >= :fromId AND id < :toId ORDER BY id")
                .param("fromId", fromId)
                .param("toId", toId)
                .query(Tweet.class)
                .list();
    }

    /** Replies to a tweet, oldest first. */
    public List<Tweet> findReplies(long tweetId) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM tweets WHERE reply_to_id = :tweetId ORDER BY created_at, id")
                .param("tweetId", tweetId)
                .query(Tweet.class)
                .list();
    }

    /**
     * Atomically adds {@code delta} to a tweet's like count (a batch of likes when write-back flushes).
     * Returns the new count, or empty if the tweet does not exist.
     */
    public Optional<Integer> addLikes(long tweetId, int delta) {
        return jdbc.sql("UPDATE tweets SET like_count = like_count + :delta WHERE id = :id RETURNING like_count")
                .param("delta", delta)
                .param("id", tweetId)
                .query(Integer.class)
                .optional();
    }
}
