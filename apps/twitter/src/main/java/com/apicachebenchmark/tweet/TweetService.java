package com.apicachebenchmark.tweet;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class TweetService {

    private final TweetRepository repository;

    public TweetService(TweetRepository repository) {
        this.repository = repository;
    }

    /**
     * Posts a tweet, or a reply when {@code replyToId} is set. The author and parent are checked by the database's
     * foreign keys rather than extra lookups, so a post costs one query.
     */
    public Tweet post(long authorId, Long replyToId, String content) {
        try {
            return repository.save(authorId, replyToId, content);
        } catch (DataIntegrityViolationException e) {
            throw new UnknownReferenceException("Author or parent tweet does not exist");
        }
    }

    public TweetWithReplies get(long id) {
        Tweet tweet = repository.findById(id).orElseThrow(() -> new TweetNotFoundException(id));
        return new TweetWithReplies(tweet, repository.findReplies(id));
    }
}
