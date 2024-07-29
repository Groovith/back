package com.groovith.groovith.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.ChatRoomStatus;
import com.groovith.groovith.domain.ChatRoomType;
import com.groovith.groovith.dto.ChatRoomDetailsDto;
import com.groovith.groovith.dto.ChatRoomListResponseDto;
import com.groovith.groovith.dto.CreateChatRoomRequestDto;
import com.groovith.groovith.dto.EnterChatRoomRequestDto;
import com.groovith.groovith.service.ChatRoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ChatRoomController.class)
@AutoConfigureMockMvc
@WithMockUser
class ChatRoomControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ChatRoomService chatRoomService;

    @Test
    public void 채팅방_생성_테스트() throws Exception{
        //given
        Long userId = 1L;
        Long chatRoomId = 1L;
        CreateChatRoomRequestDto requestDto = new CreateChatRoomRequestDto();
        requestDto.setName("room");
        requestDto.setChatRoomType(ChatRoomType.SONG);
        requestDto.setChatRoomStatus(ChatRoomStatus.PUBLIC);
        requestDto.setUserId(userId);

        ChatRoom chatRoom = createChatRoom("room");
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
        //when
        when(chatRoomService.create(any(CreateChatRoomRequestDto.class)))
                .thenReturn(chatRoom);

        ResultActions actions = mockMvc.perform(
                post("/api/chatroom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf())
        );

        //then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatRoomId").value(chatRoom.getId()));
    }

    @Test
    public void 채팅방_목록_조회() throws Exception{
        //given
        // chatRoomService.findAllDesc() 의 결과로 반환할 List<ChatRoomListResponseDto> 만들기
        ChatRoom chatRoom1 = createChatRoom("room1");
        ChatRoom chatRoom2 = createChatRoom("room2");
        ChatRoom chatRoom3 = createChatRoom("room3");

        List<ChatRoom> chatRoomList = new ArrayList<>();
        chatRoomList.add(chatRoom1);
        chatRoomList.add(chatRoom2);
        chatRoomList.add(chatRoom3);

        List<ChatRoomListResponseDto> dto = new ArrayList<>();
        dto = chatRoomList.stream().map(ChatRoomListResponseDto::new)
                .collect(Collectors.toList());;

        //when
        when(chatRoomService.findAllDesc()).thenReturn(dto);

        ResultActions actions = mockMvc.perform(
                get("/api/chatroom")
                        .with(csrf())
        );

        //then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].chatRoomName").value("room1"))
                .andExpect(jsonPath("$.data[1].chatRoomName").value("room2"))
                .andExpect(jsonPath("$.data[2].chatRoomName").value("room3"));
    }

    @Test
    public void 채팅방_상세_조회() throws Exception{
        //given
        // chatRoomService.findChatRoomDetail 가 반환할 dto 생성
        Long chatRoomId = 1L;
        ChatRoom chatRoom = createChatRoom("room");
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
        ChatRoomDetailsDto detailDto = new ChatRoomDetailsDto(chatRoom);
        //when
        when(chatRoomService.findChatRoomDetail(anyLong()))
                .thenReturn(detailDto);

        ResultActions actions = mockMvc.perform(
                get("/api/chatroom/{chatRoomId}", chatRoomId)
                        .with(csrf())
        );

        //then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatRoomId").value(chatRoomId))
                .andExpect(jsonPath("$.name").value("room"));

    }

    @Test
    public void 채팅방_삭제() throws Exception{
        //given
        Long chatRoomId  = 1L;
        //when
        doNothing().when(chatRoomService).deleteChatRoom(anyLong());

        ResultActions actions = mockMvc.perform(
                delete("/api/chatroom/{chatRoomId}", chatRoomId)
                        .with(csrf())
        );
        //then
        actions.andExpect(status().isOk());
    }

    @Test
    public void 채팅방_입장() throws Exception{
        //given
        Long chatRoomId = 1L;
        Long userId = 1L;

        EnterChatRoomRequestDto requestDto = new EnterChatRoomRequestDto();
        requestDto.setUserId(userId);
        requestDto.setChatRoomId(chatRoomId);


        //when
        doNothing().when(chatRoomService).enterChatRoom(anyLong(), anyLong());

        ResultActions actions = mockMvc.perform(
                post("/api/chatroom/{chatRoomId}", chatRoomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf())
        );

        //then
        actions
                .andExpect(status().isOk());
    }

    @Test
    public void 채팅방_퇴장() throws Exception{
        //given
        Long chatRoomId = 1L;
        Long userId = 1L;

        EnterChatRoomRequestDto requestDto = new EnterChatRoomRequestDto();
        requestDto.setUserId(userId);
        requestDto.setChatRoomId(chatRoomId);

        //when

        doNothing().when(chatRoomService).leaveChatRoom(anyLong(), anyLong());

        ResultActions actions = mockMvc.perform(
                post("/api/chatroom/{chatRoomId}", chatRoomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf())
        );

        //then
        actions.andExpect(status().isOk());
    }
    ChatRoom createChatRoom(String name){
        return ChatRoom.builder()
                .name(name)
                .chatRoomStatus(ChatRoomStatus.PUBLIC)
                .chatRoomType(ChatRoomType.SONG)
                .build();
    }
}

