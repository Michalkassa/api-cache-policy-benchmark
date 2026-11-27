package com.apicachebenchmark;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import com.apicachebenchmark.like.LikeRepository;
import com.apicachebenchmark.timeline.TimelineRepository;
import com.apicachebenchmark.tweet.Tweet;
import com.apicachebenchmark.tweet.TweetRepository;
import com.apicachebenchmark.user.User;
import com.apicachebenchmark.user.UserRepository;

@SpringBootTest
@Import(PostgresTestConfig.class)
@Transactional
class RepositoryIntegrationTest {

    @Autowired UserRepository users;
    @Autowired TweetRepository tweets;
    @Autowired LikeRepository likes;
    @Autowired TimelineRepository timeline;

    @Test
    void savesAndFindsUsersAndFollows() {
        User alice = users.save("alice", "Alice");
        User bob = users.save("bob", "Bob");

        assertThat(users.findById(alice.id())).contains(alice);
        assertThat(users.follow(alice.id(), bob.id())).isTrue();
        assertThat(users.follow(alice.id(), bob.id())).isFalse();
        assertThat(users.findFolloweeIds(alice.id())).containsExactly(bob.id());
    }

    @Test
    void savesTweetsRepliesAndBlocks() {
        User alice = users.save("alice", "Alice");
        Tweet tweet = tweets.save(alice.id(), null, "hello");
        Tweet reply = tweets.save(alice.id(), tweet.id(), "reply");

        assertThat(tweets.findById(tweet.id())).contains(tweet);
        assertThat(tweet.likeCount()).isZero();
        assertThat(tweets.findReplies(tweet.id())).containsExactly(reply);
        assertThat(tweets.findByIds(List.of(reply.id(), tweet.id()))).containsExactly(tweet, reply);
        assertThat(tweets.findByIdRange(tweet.id(), reply.id())).containsExactly(tweet);
    }

    @Test
    void likesAreIdempotentAndCounted() {
        User alice = users.save("alice", "Alice");
        Tweet tweet = tweets.save(alice.id(), null, "hello");

        assertThat(likes.save(alice.id(), tweet.id())).isTrue();
        assertThat(likes.save(alice.id(), tweet.id())).isFalse();
        assertThat(likes.exists(alice.id(), tweet.id())).isTrue();
        assertThat(likes.countByTweetId(tweet.id())).isEqualTo(1);

        assertThat(tweets.addLikes(tweet.id(), 1)).contains(1);
        assertThat(tweets.addLikes(tweet.id(), 5)).contains(6);
        assertThat(tweets.addLikes(-1, 1)).isEmpty();
    }

    @Test
    void timelineShowsNewestTopLevelTweetsFromFollowees() {
        User alice = users.save("alice", "Alice");
        User bob = users.save("bob", "Bob");
        User carol = users.save("carol", "Carol");
        users.follow(alice.id(), bob.id());

        Tweet first = tweets.save(bob.id(), null, "first");
        Tweet second = tweets.save(bob.id(), null, "second");
        tweets.save(bob.id(), first.id(), "a reply");
        tweets.save(carol.id(), null, "not followed");

        assertThat(timeline.findTimeline(alice.id(), 20)).containsExactly(second, first);
        assertThat(timeline.findTimeline(alice.id(), 1)).containsExactly(second);
    }
}
