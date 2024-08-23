package com.groovith.groovith.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groovith.groovith.domain.StreamingType;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.JoinRequestDto;
import com.groovith.groovith.dto.UserDetailsResponseDto;
import com.groovith.groovith.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
@WithMockUser
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UserService userService;
    @Autowired private ObjectMapper objectMapper;

    @Test
    public void 회원가입_테스트() throws Exception{
        //given
        String username = "user";
        String password = "1234";

        JoinRequestDto joinRequestDto = new JoinRequestDto();
        joinRequestDto.setUsername(username);
        joinRequestDto.setPassword(password);

        doNothing().when(userService).join(any(JoinRequestDto.class));

        //when
        ResultActions actions = mockMvc.perform(
                post("/api/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequestDto))
                        .with(csrf())   // csrf 토큰생성,
        );

        //then
        actions
                .andExpect(status().isOk());
    }

    @Test
    public void 유저_정보_반환_테스트() throws Exception{
        //given
        String accessToken = "access";
        User user = createUser(1L, "user", "1234");
        UserDetailsResponseDto response = new UserDetailsResponseDto(user.getId(), user.getUsername());


        //when
        when(userService.getUserByAccessToken(accessToken)).thenReturn(user);
        ResultActions actions = mockMvc.perform(
                get("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("access", accessToken)
        );

        //then
        actions
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));

    }


    public User createUser(Long id, String username, String password){
        User data = new User();
        data.setId(id);
        data.setUsername(username);
        data.setPassword(password);
        data.setRole("ROLE_USER");
        data.setStreaming(StreamingType.NONE);
        return data;
    }
}