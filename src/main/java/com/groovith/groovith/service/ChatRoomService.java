package com.groovith.groovith.service;

import com.groovith.groovith.domain.*;
import com.groovith.groovith.domain.enums.ChatRoomMemberStatus;
import com.groovith.groovith.domain.enums.UserChatRoomStatus;
import com.groovith.groovith.dto.*;
import com.groovith.groovith.exception.*;
import com.groovith.groovith.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class ChatRoomService {

    @Value("${cloud.aws.s3.defaultChatRoomImageUrl}")
    private String DEFAULT_IMG_URL;
    private static final int SINGLE_NEW_MEMBER = 1;
    private static final int MAX_MEMBER = 100;
    private static final String ERROR_ONLY_MASTER_USER_CAN_CHANGE_PERMISSION = "권한 변경은 masterUser 만 가능합니다";
    private static final String ERROR_ONLY_MASTER_USER_CAN_UPDATE_CHATROOM = "채팅방 수정은 masterUser 만 가능합니다";

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final CurrentPlaylistRepository currentPlaylistRepository;

    /**
     * 채팅방 생성
     */
    public ChatRoom create(Long userId, CreateChatRoomRequestDto request) {
        ChatRoom chatRoom = chatRoomRepository.save(
                ChatRoom.builder()
                        .name(request.getName())
                        .chatRoomStatus(request.getStatus())
                        .imageUrl(DEFAULT_IMG_URL)
                        .permission(request.getPermission())
                        .build()
        );
        User user = findUserByUserId(userId);
        //masterUserId, masterUserName 설정
        chatRoom.setMasterUserInfo(user);
        // 유저 - 채팅방 연관관계 생성
        UserChatRoom.setUserChatRoom(user, chatRoom, UserChatRoomStatus.ENTER);
        // 채팅방 플레이리스트 생성
        currentPlaylistRepository.save(new CurrentPlaylist(chatRoom.getId()));

        return chatRoom;
    }

    /**
     * 채팅방 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ChatRoomListResponseDto> findAllDesc() {
        return chatRoomRepository.findAllDesc().stream()
                .map(ChatRoomListResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 내 채팅방 목록 조회
     */
    @Transactional(readOnly = true)
    public ChatRoomDetailsListDto getChatRoomsById(Long userId) {
        User user = findUserByUserId(userId);

        return new ChatRoomDetailsListDto(userChatRoomRepository.findByUserId(user.getId()).stream()
                .map(userChatRoom -> new ChatRoomDetailsDto(userChatRoom.getChatRoom()))
                .toList());
    }


    /**
     * 채팅방 상세 조회
     */
    @Transactional(readOnly = true)
    public ChatRoomDetailsDto findChatRoomDetail(Long chatRoomId) {
        return new ChatRoomDetailsDto(findChatRoomByChatRoomId(chatRoomId));
    }

    /**
     * 채팅방 수정
     * */
    public void updateChatRoom(Long chatRoomId, Long userId, UpdateChatRoomRequestDto request) {
        ChatRoom chatRoom = findChatRoomByChatRoomId(chatRoomId);
        if(!chatRoom.getMasterUserId().equals(userId)){
            throw new NotMasterUserException(ERROR_ONLY_MASTER_USER_CAN_UPDATE_CHATROOM);
        }
        if(request.getName() != null){
            chatRoom.updateName(request.getName());
        }
        if(request.getStatus() != null){
            chatRoom.updateStatus(request.getStatus());
        }
        if(request.getPermission() != null){
            chatRoom.updatePermission(request.getPermission());
        }
    }

    /**
     * 채팅방 삭제
     */
    public void deleteChatRoom(Long chatRoomId) {
        chatRoomRepository.deleteById(chatRoomId);
    }


    /**
     * 채팅방 입장
     */
    public void enterChatRoom(Long userId, Long chatRoomId) {
        // 유저, 채팅방 조회
        User user = findUserByUserId(userId);
        ChatRoom chatRoom = findChatRoomByChatRoomId(chatRoomId);
        // 채팅방 인원 제한 : 100명
        validateChatRoomCapacity(chatRoom, SINGLE_NEW_MEMBER);

        updateUserChatRoomStatus(user, chatRoom, UserChatRoomStatus.ENTER);
        chatRoom.addUser();
    }


    /**
     * 채팅방 퇴장
     */
    public ChatRoomMemberStatus leaveChatRoom(Long userId, Long chatRoomId) {
        ChatRoom chatRoom = findChatRoomByChatRoomId(chatRoomId);

        updateUserChatRoomStatus(findUserByUserId(userId), chatRoom, UserChatRoomStatus.LEAVE);
        chatRoom.subUser();
        // 유저 퇴장시, 채팅방이 비어있다면 현재 채팅방 삭제
        return deleteChatRoomWhenEmpty(chatRoom);
    }


    @Transactional(readOnly = true)
    public List<UserChatRoomDto> findAllUser(Long chatRoomId) {
        ChatRoom chatRoom = findChatRoomByChatRoomId(chatRoomId);

        return chatRoom.getUserChatRooms().stream()
                .map(userChatRoom -> userChatRoom.getUser().toUserChatRoomDto(userChatRoom.getUser()))
                .collect(Collectors.toList());
    }

    /**
     * 채팅방으로 초대 - 현재 사용 x
     */
    public void invite(Long inviterId, Long inviteeId, Long chatRoomId) {
        //User inviter = findByUserById(inviterId);
        User invitee = findUserByUserId(inviteeId);
        ChatRoom chatRoom = findChatRoomByChatRoomId(chatRoomId);

        // 초대받은 유저와 채팅방 연관관계 생성
        Optional<UserChatRoom> userChatRoom = findUserChatRoomByUserIdAndChatRoomId(inviteeId, chatRoomId);
        updateUserChatRoomStatus(invitee, chatRoom, UserChatRoomStatus.ENTER);
        chatRoom.addUser();
    }

    /**
     * 채팅방으로 친구 초대
     * */
    public void inviteFriends(Long chatRoomId, List<Long> friendsIdList) {
        ChatRoom chatRoom = findChatRoomByChatRoomId(chatRoomId);
        // 친구 초대시 최대인원 초과하는지 검증
        validateChatRoomCapacity(chatRoom, friendsIdList.size());
        // 초대받은 각 친구마다 채팅방과 연관관계 생성
        for (Long friendsId : friendsIdList) {
            updateUserChatRoomStatus(findUserByUserId(friendsId), chatRoom, UserChatRoomStatus.ENTER);
            chatRoom.addUser();
        }
    }

    /**
     * 채팅방 권한 변경
     */
    public void updatePermission(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = findChatRoomByChatRoomId(chatRoomId);
        // mastUserId == userId 인 경우만 권한 변경 가능
        validateMasterUser(userId, chatRoom.getMasterUserId(), ERROR_ONLY_MASTER_USER_CAN_CHANGE_PERMISSION);

        chatRoom.changePermission();
    }


    private ChatRoom findChatRoomByChatRoomId(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));
    }

    private User findUserByUserId(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
    
    private Optional<UserChatRoom> findUserChatRoomByUserIdAndChatRoomId(Long userId, Long chatRoomId) {
        return userChatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
    }

    private void validateChatRoomCapacity(ChatRoom chatRoom, int newEnterMemberCount) {
        if (chatRoom.getCurrentMemberCount() + newEnterMemberCount > MAX_MEMBER) {
            throw new ChatRoomFullException(chatRoom.getId());
        }
    }

    private void validateUserAlreadyInChatRoom(Long userId, Long chatRoomId, UserChatRoom userChatRoom) {
        if (userChatRoom.getStatus() == UserChatRoomStatus.ENTER) {
            // 초대받은 유저가 채팅방에 이미 참가중인지 확인
            throw new UserAlreadyInChatRoomException(userId, chatRoomId);
        }
    }

    private void validateMasterUser(Long userId, Long masterUserId, String errorMessage) {
        if (masterUserId.equals(userId)) {
            throw new NotMasterUserException(errorMessage);
        }
    }

    private void updateUserChatRoomStatus(User user, ChatRoom chatRoom, UserChatRoomStatus status) {
        Optional<UserChatRoom> userChatRoom = findUserChatRoomByUserIdAndChatRoomId(user.getId(), chatRoom.getId());

        if (userChatRoom.isEmpty()) {
            UserChatRoom.setUserChatRoom(user, chatRoom, status);
            return;
        }

        if (!userChatRoom.get().getStatus().equals(status)) {
            userChatRoom.get().setStatus(status);
        }
    }

    private ChatRoomMemberStatus deleteChatRoomWhenEmpty(ChatRoom chatRoom){
        if (chatRoom.getCurrentMemberCount() <= 0) {
            // 채팅방 플레이리스트 함께 삭제
            currentPlaylistRepository.deleteByChatRoomId(chatRoom.getId());
            return ChatRoomMemberStatus.EMPTY;
        }
        return ChatRoomMemberStatus.ACTIVE;
    }
}