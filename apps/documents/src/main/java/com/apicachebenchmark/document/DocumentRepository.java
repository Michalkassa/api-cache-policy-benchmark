package com.apicachebenchmark.document;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentRepository {

    private static final String COLUMNS = "id, title, content, version, updated_at";

    private final JdbcClient jdbc;

    public DocumentRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Document save(String title, String content) {
        return jdbc.sql("INSERT INTO documents (title, content) VALUES (:title, :content) RETURNING " + COLUMNS)
                .param("title", title)
                .param("content", content)
                .query(Document.class)
                .single();
    }

    public Optional<Document> findById(long id) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM documents WHERE id = :id")
                .param("id", id)
                .query(Document.class)
                .optional();
    }

    /** Documents with {@code fromId <= id < toId}: loads one cache block ({@code block:{id / size}}). */
    public List<Document> findByIdRange(long fromId, long toId) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM documents WHERE id >= :fromId AND id < :toId ORDER BY id")
                .param("fromId", fromId)
                .param("toId", toId)
                .query(Document.class)
                .list();
    }

    /**
     * Replaces a document's content and bumps its version atomically (invalidate and write-through).
     * Returns the new version, or empty if the document does not exist.
     */
    public Optional<Long> updateContent(long id, String content) {
        return jdbc.sql("""
                        UPDATE documents SET content = :content, version = version + 1, updated_at = now()
                        WHERE id = :id
                        RETURNING version""")
                .param("content", content)
                .param("id", id)
                .query(Long.class)
                .optional();
    }

    /**
     * Applies an edit whose version was already assigned elsewhere (write-back flush from Redis).
     * Ignored unless {@code version} is newer than the stored one, so late or repeated flushes are safe.
     * Returns true if the row changed.
     */
    public boolean applyContent(long id, String content, long version) {
        return jdbc.sql("""
                        UPDATE documents SET content = :content, version = :version, updated_at = now()
                        WHERE id = :id AND version < :version""")
                .param("content", content)
                .param("version", version)
                .param("id", id)
                .update() == 1;
    }
}
