package com.groovith.groovith.service;

import com.groovith.groovith.domain.*;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.NotificationRepository;
import com.groovith.groovith.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @InjectMocks NotificationService notificationService;
    @Mock NotificationRepository notificationRepository;
    @Mock UserRepository userRepository;
    @Mock ChatRoomRepository chatRoomRepository;

    @Test
    @DisplayName("알람 저장 테스트")
    public void save(){
        //given
        String alarm = "alarm message";
        Long userId = 1L;
        User user = createUser(userId, "user1", "1234");
        Notification notification = Notification.builder()
                .alarm(alarm).build();

        //when
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        Notification savedNotification = notificationService.save(alarm, user);

        //then
        // 유저의 알람 필드에 저장됐는지 테스트
        Assertions.assertThat(user.getNotifications().get(0)).isEqualTo(savedNotification);
        Assertions.assertThat(savedNotification.getAlarm()).isEqualTo(alarm);
    }

    @Test
    @DisplayName("초대 메세지 생성 테스트")
    public void createInviteNotification(){
        //given
        Long inviteeId = 100L;
        Long inviterId = 1L;
        Long chatRoomId = 1L;
        User invitee = createUser(inviteeId, "invitee", "1234");
        User inviter = createUser(inviterId, "inviter", "1234");

        ChatRoom chatRoom = createChatRoom("room", ChatRoomStatus.PUBLIC);
        ReflectionTestUtils.setField(chatRoom, "id", chatRoomId);
        // 생성될 알림 메세지
        String data = inviter.getUsername() + " 이(가) " + invitee.getUsername() + " 을(를) " + chatRoom.getName() + " 에 초대했습니다";

        //when
        when(userRepository.findById(inviteeId)).thenReturn(Optional.of(invitee));
        when(userRepository.findById(inviterId)).thenReturn(Optional.of(inviter));
        when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));

        String alarm = notificationService.createInviteNotification(inviteeId, inviterId, chatRoomId);

        //then
        Assertions.assertThat(alarm).isEqualTo(data);
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

    ChatRoom createChatRoom(String name, ChatRoomStatus chatRoomStatus){
        return ChatRoom.builder()
                .name(name)
                .chatRoomStatus(chatRoomStatus)
                .build();
    }
}