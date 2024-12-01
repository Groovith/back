package com.groovith.groovith.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.domain.enums.ChatRoomPermission;
import com.groovith.groovith.domain.enums.ChatRoomPrivacy;
import com.groovith.groovith.domain.enums.S3Directory;
import com.groovith.groovith.dto.ChatRoomDetailsDto;
import com.groovith.groovith.dto.CreateChatRoomRequestDto;
import com.groovith.groovith.dto.EnterChatRoomRequestDto;
import com.groovith.groovith.security.CustomUserDetails;
import com.groovith.groovith.security.JwtFilter;
import com.groovith.groovith.service.ChatRoomService;
import com.groovith.groovith.service.Image.ImageService;
import com.groovith.groovith.service.MessageService;
import com.groovith.groovith.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ChatRoomController.class)
@MockBean(JpaMetamodelMappingContext.class)
@WithMockUser
class ChatRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ChatRoomService chatRoomService;
    @MockBean
    private ImageService chatRoomImageService;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private SimpMessageSendingOperations template;
    @MockBean
    private MessageService messageService;
    @MockBean
    private JwtFilter jwtFilter;

    @Test
    public void 채팅방_생성_테스트() throws Exception {
        //given
        Long chatRoomId = 1L;
        Long userId = 1L;
        String chatRoomName = "test";
        CreateChatRoomRequestDto requestDto = new CreateChatRoomRequestDto();
        ReflectionTestUtils.setField(requestDto, "name", chatRoomName);
        ReflectionTestUtils.setField(requestDto, "privacy", ChatRoomPrivacy.PUBLIC);
        ReflectionTestUtils.setField(requestDto, "permission", ChatRoomPermission.MASTER);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        String testImageUrl = "imageUrl";
        ChatRoom chatRoom = ChatRoom.builder()
                .name(requestDto.getName())
                .privacy(requestDto.getPrivacy())
                .imageUrl(testImageUrl)
                .permission(requestDto.getPermission())
                .build();
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);

        //when
        when(chatRoomImageService.uploadAndSaveImage(any(file.getClass())))
                .thenReturn(testImageUrl);
        when(chatRoomService.create(userId, requestDto, testImageUrl)).thenReturn(chatRoom);

        ResultActions actions = mockMvc.perform(
                multipart("/api/chatrooms")
                        .file(file)
                        .param("dto", new ObjectMapper().writeValueAsString(requestDto))
                        .with(SecurityMockMvcRequestPostProcessors.user(new CustomUserDetails(createUser(userId, "user"))))
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA)
        );

        //then
        actions
                .andExpect(status().isOk());
    }

//    @Test
//    public void 채팅방_목록_조회() throws Exception{
//        //given
//        // chatRoomService.findAllDesc() 의 결과로 반환할 List<ChatRoomListResponseDto> 만들기
//        ChatRoom chatRoom1 = createChatRoom("room1", ChatRoomStatus.PUBLIC);
//        ChatRoom chatRoom2 = createChatRoom("room2", ChatRoomStatus.PUBLIC);
//        ChatRoom chatRoom3 = createChatRoom("room3", ChatRoomStatus.PUBLIC);
//
//        List<ChatRoom> chatRoomList = new ArrayList<>();
//        chatRoomList.add(chatRoom1);
//        chatRoomList.add(chatRoom2);
//        chatRoomList.add(chatRoom3);
//
//        List<ChatRoomListResponseDto> dto = new ArrayList<>();
//        dto = chatRoomList.stream().map(ChatRoomListResponseDto::new)
//                .collect(Collectors.toList());;
//
//        //when
//        when(chatRoomService.findAllDesc()).thenReturn(dto);
//
//        ResultActions actions = mockMvc.perform(
//                get("/api/chatrooms")
//                        .with(csrf())
//        );
//
//        //then
//        actions
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.data[0].chatRoomName").value("room1"))
//                .andExpect(jsonPath("$.data[1].chatRoomName").value("room2"))
//                .andExpect(jsonPath("$.data[2].chatRoomName").value("room3"));
//    }

    @Test
    public void 채팅방_상세_조회() throws Exception {
        //given
        // chatRoomService.findChatRoomDetail 가 반환할 dto 생성
        Long chatRoomId = 1L;
        Long userId = 1L;
        ChatRoom chatRoom = createChatRoom("room", ChatRoomPrivacy.PUBLIC, ChatRoomPermission.EVERYONE);
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
        ChatRoomDetailsDto detailDto = new ChatRoomDetailsDto(chatRoom, chatRoom.getIsMaster(userId));
        //when
        when(chatRoomService.findChatRoomDetails(chatRoomId, userId))
                .thenReturn(detailDto);

        ResultActions actions = mockMvc.perform(
                get("/api/chatrooms/{chatRoomId}", chatRoomId)
                        .with(csrf())
        );

        //then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatRoomId").value(chatRoomId))
                .andExpect(jsonPath("$.name").value("room"));

    }

    @Test
    public void 채팅방_삭제() throws Exception {
        //given
        Long chatRoomId = 1L;
        //when
        doNothing().when(chatRoomService).deleteChatRoom(anyLong(), 1L);

        ResultActions actions = mockMvc.perform(
                delete("/api/chatroom/{chatRoomId}", chatRoomId)
                        .with(csrf())
        );
        //then
        actions.andExpect(status().isOk());
    }

    @Test
    public void 채팅방_입장() throws Exception {
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
    public void 채팅방_퇴장() throws Exception {
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

    private ChatRoom createChatRoom(String name, ChatRoomPrivacy privacy, ChatRoomPermission permission) {
        return ChatRoom.builder()
                .name(name)
                .privacy(privacy)
                .permission(permission)
                .build();
    }

    private User createUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setUsername(name);
        user.setPassword("password");
        user.setImageUrl(S3Directory.USER.getDefaultImageUrl());
        return user;
    }
}

