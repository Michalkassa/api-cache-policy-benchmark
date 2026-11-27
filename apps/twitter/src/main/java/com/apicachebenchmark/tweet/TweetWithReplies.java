package com.apicachebenchmark.tweet;

import java.util.List;

public record TweetWithReplies(Tweet tweet, List<Tweet> replies) {
}
