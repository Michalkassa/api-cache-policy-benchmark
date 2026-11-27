package com.apicachebenchmark.tweet;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TweetNotFoundException extends RuntimeException {

    public TweetNotFoundException(long id) {
        super("Tweet " + id + " does not exist");
    }
}
