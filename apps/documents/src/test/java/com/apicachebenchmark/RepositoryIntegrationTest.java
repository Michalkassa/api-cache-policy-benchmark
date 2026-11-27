package com.apicachebenchmark;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.apicachebenchmark.document.Document;
import com.apicachebenchmark.document.DocumentRepository;

@SpringBootTest
@Import(PostgresTestConfig.class)
@Transactional
class RepositoryIntegrationTest {

    @Autowired DocumentRepository documents;

    @Test
    void savesAndFindsDocumentsAndBlocks() {
        Document first = documents.save("First", "the quick brown fox");
        Document second = documents.save("Second", "jumps over the lazy dog");

        assertThat(first.version()).isEqualTo(1);
        assertThat(documents.findById(first.id())).contains(first);
        assertThat(documents.findById(-1)).isEmpty();
        assertThat(documents.findByIdRange(first.id(), second.id() + 1)).containsExactly(first, second);
        assertThat(documents.findByIdRange(first.id(), second.id())).containsExactly(first);
    }

    @Test
    void editsBumpTheVersion() {
        Document doc = documents.save("Doc", "v1");

        assertThat(documents.updateContent(doc.id(), "v2")).contains(2L);
        assertThat(documents.updateContent(-1, "v2")).isEmpty();
        assertThat(documents.findById(doc.id()).orElseThrow().content()).isEqualTo("v2");
    }

    @Test
    void writeBackFlushAppliesOnlyNewerVersions() {
        Document doc = documents.save("Doc", "v1");

        assertThat(documents.applyContent(doc.id(), "v5", 5)).isTrue();
        assertThat(documents.applyContent(doc.id(), "v4", 4)).isFalse();
        assertThat(documents.applyContent(doc.id(), "v5 again", 5)).isFalse();

        Document stored = documents.findById(doc.id()).orElseThrow();
        assertThat(stored.content()).isEqualTo("v5");
        assertThat(stored.version()).isEqualTo(5);
    }
}
