package com.apicachebenchmark.tweet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.apicachebenchmark.PostgresTestConfig;
import com.apicachebenchmark.user.User;
import com.apicachebenchmark.user.UserRepository;
import com.jayway.jsonpath.JsonPath;

/** POST /tweets and GET /tweets/{id} end to end, against a real PostgreSQL. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(PostgresTestConfig.class)
@Transactional
class TweetApiIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;

    @Test
    void postedTweetAndReplyCanBeRead() throws Exception {
        User alice = users.save("alice", "Alice");
        User bob = users.save("bob", "Bob");

        long tweetId = postTweet("{\"authorId\": " + alice.id() + ", \"content\": \"hello\"}");
        long replyId = postTweet("{\"authorId\": " + bob.id() + ", \"replyToId\": " + tweetId + ", \"content\": \"hi\"}");

        mvc.perform(get("/tweets/" + tweetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tweet.content").value("hello"))
                .andExpect(jsonPath("$.tweet.authorId").value(alice.id()))
                .andExpect(jsonPath("$.replies.length()").value(1))
                .andExpect(jsonPath("$.replies[0].id").value(replyId))
                .andExpect(jsonPath("$.replies[0].content").value("hi"));
    }

    @Test
    void postingAsUnknownAuthorReturns422() throws Exception {
        mvc.perform(post("/tweets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\": 999999, \"content\": \"hello\"}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void replyingToUnknownTweetReturns422() throws Exception {
        User alice = users.save("alice", "Alice");

        mvc.perform(post("/tweets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"authorId\": " + alice.id() + ", \"replyToId\": 999999, \"content\": \"hi\"}"))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void unknownTweetReturns404() throws Exception {
        mvc.perform(get("/tweets/999999")).andExpect(status().isNotFound());
    }

    private long postTweet(String body) throws Exception {
        String response = mvc.perform(post("/tweets").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }
}
