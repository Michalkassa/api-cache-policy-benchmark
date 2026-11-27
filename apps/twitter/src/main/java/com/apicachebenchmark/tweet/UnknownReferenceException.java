package com.apicachebenchmark.tweet;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** The request is well formed but names an author or parent tweet that does not exist. */
@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class UnknownReferenceException extends RuntimeException {

    public UnknownReferenceException(String message) {
        super(message);
    }
}
