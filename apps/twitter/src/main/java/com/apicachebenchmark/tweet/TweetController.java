package com.apicachebenchmark.tweet;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tweets")
public class TweetController {

    private final TweetService service;

    public TweetController(TweetService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Tweet> post(@Valid @RequestBody PostTweetRequest request, UriComponentsBuilder uri) {
        Tweet tweet = service.post(request.authorId(), request.replyToId(), request.content());
        return ResponseEntity.created(uri.path("/tweets/{id}").build(tweet.id())).body(tweet);
    }

    @GetMapping("/{id}")
    public TweetWithReplies get(@PathVariable long id) {
        return service.get(id);
    }
}
