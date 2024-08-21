package com.groovith.groovith.service;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.Notification;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.CreateFollowNotificationResponseDto;
import com.groovith.groovith.exception.ChatRoomNotFoundException;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.NotificationRepository;
import com.groovith.groovith.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;


    /**
     * 초대 알림 메세지 생성 + 저장
     */
    public String createInviteNotification(Long inviteeId, Long inviterId, Long chatRoomId) {
        User invitee = userRepository.findById(inviteeId)
                .orElseThrow(() -> new UserNotFoundException(inviteeId));

        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new UserNotFoundException(inviteeId));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));

        String alarm = inviter.getUsername() + " 님이 " + invitee.getUsername() + " 님을 " + chatRoom.getName() + " 에 초대했습니다";

        save(alarm, invitee);
        return alarm;
    }

    /**
     * 팔로우 알림 메세지 생성 + 저장
     * */
    public CreateFollowNotificationResponseDto createFollowNotification(String followerName, String followingName){
        User follower = userRepository.findByUsername(followerName)
                .orElseThrow(()-> new UserNotFoundException(followerName));

        User following = userRepository.findByUsername(followingName)
                .orElseThrow(()-> new UserNotFoundException(followingName));

        String alarm = follower.getUsername() + "님이" + following.getUsername() + "님에게 팔로우 요청을 보냈습니다";

        CreateFollowNotificationResponseDto responseDto = new CreateFollowNotificationResponseDto();
        responseDto.setFollowingId(following.getId());
        responseDto.setAlarm(alarm);
        // 팔로우 받은 사람에게 알림
        save(alarm, following);
        return responseDto;
    }


    /**
     * 알림 저장
     * */
    public Notification save(String alarm, User user){
        Notification notification = Notification.builder()
                .alarm(alarm)
                .build();
        notification.setNotification(user);
        notificationRepository.save(notification);
        return notification;
    }
}
