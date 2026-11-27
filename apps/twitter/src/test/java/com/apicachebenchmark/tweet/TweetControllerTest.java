package com.apicachebenchmark.tweet;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TweetController.class)
class TweetControllerTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-11-27T12:00:00Z");

    @Autowired MockMvc mvc;
    @MockitoBean TweetService service;

    @Test
    void postReturnsCreatedTweet() throws Exception {
        when(service.post(7, null, "hello")).thenReturn(new Tweet(1, 7, null, "hello", 0, NOW));

        mvc.perform(post("/tweets").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorId": 7, "content": "hello"}"""))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/tweets/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.authorId").value(7))
                .andExpect(jsonPath("$.content").value("hello"))
                .andExpect(jsonPath("$.likeCount").value(0));
    }

    @Test
    void postAcceptsAReply() throws Exception {
        when(service.post(7, 1L, "a reply")).thenReturn(new Tweet(2, 7, 1L, "a reply", 0, NOW));

        mvc.perform(post("/tweets").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorId": 7, "replyToId": 1, "content": "a reply"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.replyToId").value(1));
    }

    @Test
    void postRejectsBlankContent() throws Exception {
        mvc.perform(post("/tweets").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorId": 7, "content": "   "}"""))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void postRejectsContentOver280Characters() throws Exception {
        String tooLong = "x".repeat(281);

        mvc.perform(post("/tweets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\": 7, \"content\": \"" + tooLong + "\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void postRejectsMissingAuthor() throws Exception {
        mvc.perform(post("/tweets").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "hello"}"""))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void postReturns422ForUnknownAuthorOrParent() throws Exception {
        when(service.post(anyLong(), any(), anyString()))
                .thenThrow(new UnknownReferenceException("Author or parent tweet does not exist"));

        mvc.perform(post("/tweets").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorId": 99, "content": "hello"}"""))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void getReturnsTweetWithReplies() throws Exception {
        Tweet tweet = new Tweet(1, 7, null, "hello", 3, NOW);
        Tweet reply = new Tweet(2, 8, 1L, "a reply", 0, NOW);
        when(service.get(1)).thenReturn(new TweetWithReplies(tweet, List.of(reply)));

        mvc.perform(get("/tweets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tweet.id").value(1))
                .andExpect(jsonPath("$.tweet.likeCount").value(3))
                .andExpect(jsonPath("$.replies.length()").value(1))
                .andExpect(jsonPath("$.replies[0].id").value(2));
    }

    @Test
    void getReturns404ForUnknownTweet() throws Exception {
        when(service.get(42)).thenThrow(new TweetNotFoundException(42));

        mvc.perform(get("/tweets/42"))
                .andExpect(status().isNotFound());
    }
}
