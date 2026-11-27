package com.apicachebenchmark.user;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private static final String COLUMNS = "id, username, display_name, created_at";

    private final JdbcClient jdbc;

    public UserRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public User save(String username, String displayName) {
        return jdbc.sql("INSERT INTO users (username, display_name) VALUES (:username, :displayName) RETURNING " + COLUMNS)
                .param("username", username)
                .param("displayName", displayName)
                .query(User.class)
                .single();
    }

    public Optional<User> findById(long id) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM users WHERE id = :id")
                .param("id", id)
                .query(User.class)
                .optional();
    }

    /** Returns false if the follow already existed. */
    public boolean follow(long followerId, long followeeId) {
        return jdbc.sql("""
                        INSERT INTO follows (follower_id, followee_id) VALUES (:followerId, :followeeId)
                        ON CONFLICT DO NOTHING""")
                .param("followerId", followerId)
                .param("followeeId", followeeId)
                .update() == 1;
    }

    public List<Long> findFolloweeIds(long followerId) {
        return jdbc.sql("SELECT followee_id FROM follows WHERE follower_id = :followerId")
                .param("followerId", followerId)
                .query(Long.class)
                .list();
    }
}
