package com.groovith.groovith.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.FollowRequest;
import com.groovith.groovith.dto.FollowResponse;
import com.groovith.groovith.service.FollowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyChar;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FollowController.class)
@AutoConfigureMockMvc
@WithMockUser
class FollowControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private FollowService followService;
    @Autowired private ObjectMapper objectMapper;

    @Test
    public void 팔로우_테스트() throws Exception{
        //given
        FollowRequest request = new FollowRequest();
        String follower = "follower";
        String following = "following";
        request.setFollower(follower);
        request.setFollowing(following);

        //when
        when(followService.follow(any(String.class), any(String.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResultActions actions = mockMvc.perform(
                post("/api/follow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk());
    }

    @Test
    public void 언팔로우_테스트() throws Exception{
        //given
        FollowRequest request = new FollowRequest();
        String follower = "follower";
        String following = "following";
        request.setFollower(follower);
        request.setFollowing(following);

        //when
        when(followService.follow(any(String.class), any(String.class)))
                .thenReturn(ResponseEntity.ok().build());

        ResultActions actions = mockMvc.perform(
                post("/api/unfollow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk());

    }

    @Test
    public void 팔로잉_목록_조회_테스트() throws Exception{
        //given
        String username = "user";
        FollowResponse response = new FollowResponse();

        //when
        when(followService.getFollowing(any(String.class))).thenReturn(any(FollowResponse.class));

        ResultActions actions = mockMvc.perform(
                get("/api/{username}/following", username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(username)
        );

        //then
        actions.andExpect(status().isOk());
    }

    @Test
    public void 팔로워_목록_조회_테스트() throws Exception {
        //given
        String username = "user";
        FollowResponse response = new FollowResponse();
        //when
        when(followService.getFollowers(any(String.class))).thenReturn(any(FollowResponse.class));

        ResultActions actions = mockMvc.perform(
                get("/api/{username}/followers", username)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(username)
        );

        //then
        actions.andExpect(status().isOk());
    }


}