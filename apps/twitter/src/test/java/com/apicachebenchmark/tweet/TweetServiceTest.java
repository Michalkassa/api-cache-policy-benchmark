package com.apicachebenchmark.tweet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class TweetServiceTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-11-27T12:00:00Z");

    private TweetRepository repository;
    private TweetService service;

    @BeforeEach
    void setUp() {
        repository = mock(TweetRepository.class);
        service = new TweetService(repository);
    }

    @Test
    void postSavesTheTweet() {
        Tweet saved = new Tweet(1, 7, null, "hello", 0, NOW);
        when(repository.save(7, null, "hello")).thenReturn(saved);

        assertThat(service.post(7, null, "hello")).isEqualTo(saved);
    }

    @Test
    void postSavesAReply() {
        Tweet reply = new Tweet(2, 7, 1L, "a reply", 0, NOW);
        when(repository.save(7, 1L, "a reply")).thenReturn(reply);

        assertThat(service.post(7, 1L, "a reply")).isEqualTo(reply);
    }

    @Test
    void postRejectsAnUnknownAuthorOrParent() {
        when(repository.save(99, null, "hello")).thenThrow(new DataIntegrityViolationException("fk"));

        assertThatThrownBy(() -> service.post(99, null, "hello"))
                .isInstanceOf(UnknownReferenceException.class);
    }

    @Test
    void getReturnsTheTweetWithItsReplies() {
        Tweet tweet = new Tweet(1, 7, null, "hello", 3, NOW);
        Tweet reply = new Tweet(2, 8, 1L, "a reply", 0, NOW);
        when(repository.findById(1)).thenReturn(Optional.of(tweet));
        when(repository.findReplies(1)).thenReturn(List.of(reply));

        assertThat(service.get(1)).isEqualTo(new TweetWithReplies(tweet, List.of(reply)));
    }

    @Test
    void getThrowsWhenTheTweetDoesNotExist() {
        when(repository.findById(42)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(42)).isInstanceOf(TweetNotFoundException.class);
        verify(repository, never()).findReplies(anyLong());
    }
}
