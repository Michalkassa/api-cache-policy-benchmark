package com.apicachebenchmark.tweet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostTweetRequest(
        @NotNull Long authorId,
        Long replyToId,
        @NotBlank @Size(max = 280) String content) {
}
