package com.groovith.groovith.service;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.exception.ChatRoomNotFoundException;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.repository.ChatRoomRepository;
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

    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    public String createInviteNotification(Long inviteeId, Long inviterId, Long chatRoomId){
        User invitee = userRepository.findById(inviteeId)
                .orElseThrow(()-> new UserNotFoundException(inviteeId));

        User inviter = userRepository.findById(inviterId)
                .orElseThrow(()-> new UserNotFoundException(inviteeId));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(()-> new ChatRoomNotFoundException(chatRoomId));

        return invitee.getUsername() + " 이(가) " + inviter.getUsername() + " 을 " + chatRoom.getName() + " 에 초대했습니다";
    }

}
