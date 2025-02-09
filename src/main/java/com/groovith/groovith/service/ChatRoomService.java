package com.groovith.groovith.service;

import com.groovith.groovith.domain.*;
import com.groovith.groovith.domain.enums.ChatRoomMemberStatus;
import com.groovith.groovith.domain.enums.UserChatRoomStatus;
import com.groovith.groovith.domain.enums.UserRelationship;
import com.groovith.groovith.dto.*;
import com.groovith.groovith.exception.*;
import com.groovith.groovith.repository.*;
import com.groovith.groovith.service.Image.ChatRoomImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private static final int ZSet_INDEX_START = 0;
    private static final int ZSet_INDEX_END = 10;
    private static final int SINGLE_NEW_MEMBER = 1;
    private static final int MAX_MEMBER = 100;
    private static final String ERROR_ONLY_MASTER_USER_CAN_CHANGE_PERMISSION = "권한 변경은 masterUser 만 가능합니다";
    private static final String ERROR_ONLY_MASTER_USER_CAN_UPDATE_CHATROOM = "채팅방 수정은 masterUser 만 가능합니다";
    private static final String ERROR_ONLY_MASTER_USER_CAN_DELETE_CHATROOM = "채팅방 삭제는 masterUser 만 가능합니다";

    private final ChatRoomImageService chatRoomImageService;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final CurrentPlaylistRepository currentPlaylistRepository;
    private final FriendRepository friendRepository;
    private final MessageRepository messageRepository;
    private final PlayerSessionService playerSessionService;

    /**
     * 채팅방 생성
     */
    public ChatRoom create(Long userId, CreateChatRoomRequestDto request, String imageUrl) {
        ChatRoom chatRoom = chatRoomRepository.save(
                ChatRoom.builder()
                        .name(request.getName())
                        .privacy(request.getPrivacy())
                        .imageUrl(imageUrl)
                        .permission(request.getPermission())
                        .build()
        );
        User user = findUserById(userId);
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
        List<UserChatRoom> enterUserChatRooms = userChatRoomRepository.findEnterChatRoomsByUserId(userId, UserChatRoomStatus.ENTER);

        return new ChatRoomDetailsListDto(enterUserChatRooms.stream()
                .map(userChatRoom -> {
                    ChatRoom chatRoom = userChatRoom.getChatRoom();
                    return createChatRoomDetailsDto(chatRoom, chatRoom.getIsMaster(userId));
                })
                .toList());
    }


    /**
     * 채팅방 상세 조회
     */
    @Transactional(readOnly = true)
    public ResponseEntity<ChatRoomDetailsDto> findChatRoomDetails(Long chatRoomId, Long userId) {
        if (!isMember(chatRoomId, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        return ResponseEntity.ok().body(createChatRoomDetailsDto(chatRoom, chatRoom.getIsMaster(userId)));
    }

    public boolean isMember(Long chatRoomId, Long userId) {
        return userChatRoomRepository.existsByChatRoomIdAndUserId(chatRoomId, userId);
    }

    /**
     * 채팅방 수정
     */
    public void updateChatRoom(Long chatRoomId, Long userId, UpdateChatRoomRequestDto request, String imageUrl) {
        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        validateMasterUser(userId, chatRoom.getMasterUserId(), ERROR_ONLY_MASTER_USER_CAN_UPDATE_CHATROOM);
        chatRoom.update(request.getName(), request.getPrivacy(), request.getPermission(), imageUrl);
    }

    /**
     * 채팅방 삭제
     */
    public ResponseEntity<? super DeleteChatRoomResponseDto> deleteChatRoom(Long chatRoomId, Long userId) {
        try {
            ChatRoom chatRoom = findChatRoomById(chatRoomId);
            deleteChatRoomData(chatRoomId, userId, chatRoom.getMasterUserId());
        } catch (NotMasterUserException e) {
            return DeleteChatRoomResponseDto.notMasterUser();
        }
        return DeleteChatRoomResponseDto.success();
    }

    /**
     * 채팅방 입장
     */
    public void enterChatRoom(Long userId, Long chatRoomId) {
        // 유저, 채팅방 조회
        User user = findUserById(userId);
        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        // 채팅방 인원 제한 : 100명
        validateChatRoomCapacity(chatRoom, SINGLE_NEW_MEMBER);

        updateUserChatRoomStatus(user, chatRoom, UserChatRoomStatus.ENTER);
        chatRoom.increaseMemberCount();
    }


    /**
     * 채팅방 퇴장
     */
    public ResponseEntity<?> leaveChatRoom(Long userId, Long chatRoomId) {
        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        ChatRoomMemberStatus chatRoomMemberStatus = updateChatRoomMemberStatusOnLeave(userId, chatRoom);
        // 유저 퇴장시, 채팅방이 비어있다면 현재 채팅방 삭제, 방장인 경우 bad Request
        return validateAndHandleChatRoomMemberStatus(userId, chatRoom, chatRoomMemberStatus);
    }

    private ResponseEntity<?> validateAndHandleChatRoomMemberStatus(Long userId, ChatRoom chatRoom, ChatRoomMemberStatus chatRoomMemberStatus) {
        switch (chatRoomMemberStatus) {
            case MASTER_LEAVING -> {    // 방장이 나가는 경우
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            case EMPTY -> {             // 채팅방 비어있을 경우 채팅방 삭제
                deleteChatRoom(chatRoom.getId(), userId);
                break;
            }
            case ACTIVE -> {            // 정상적인 경우 userChatRoom 업데이트
                updateUserChatRoomStatus(findUserById(userId), chatRoom, UserChatRoomStatus.LEAVE);
                chatRoom.decreaseMemberCount();
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 채팅방의 현재 멤버 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ChatRoomMemberDto> findChatRoomMembers(Long chatRoomId, Long userId) {
        User user = findUserById(userId);
        Set<Long> friendsIdsFromUser = new HashSet<>(friendRepository.findFriendsIdsFromUser(user));
        //fetch join
        List<UserChatRoom> enterUserChatRooms = userChatRoomRepository.findEnterUserChatRoomsByChatRoomId(chatRoomId, UserChatRoomStatus.ENTER);

        return enterUserChatRooms.stream()
                .map(userChatRoom -> creatChatRoomMemberDto(user, userChatRoom.getUser(), friendsIdsFromUser))
                .toList();
    }

    /**
     * 채팅방으로 초대 - 현재 사용 x
     */
    public void invite(Long inviterId, Long inviteeId, Long chatRoomId) {
        //User inviter = findByUserById(inviterId);
        User invitee = findUserById(inviteeId);
        ChatRoom chatRoom = findChatRoomById(chatRoomId);

        // 초대받은 유저와 채팅방 연관관계 생성
        Optional<UserChatRoom> userChatRoom = findUserChatRoomByUserIdAndChatRoomId(inviteeId, chatRoomId);
        updateUserChatRoomStatus(invitee, chatRoom, UserChatRoomStatus.ENTER);
        chatRoom.increaseMemberCount();
    }

    /**
     * 채팅방으로 친구 초대
     */
    public void inviteFriends(Long chatRoomId, List<Long> friendsIdList) {
        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        // 친구 초대시 최대인원 초과하는지 검증
        validateChatRoomCapacity(chatRoom, friendsIdList.size());
        // 초대받은 각 친구마다 채팅방과 연관관계 생성
        for (Long friendsId : friendsIdList) {
            updateUserChatRoomStatus(findUserById(friendsId), chatRoom, UserChatRoomStatus.ENTER);
            chatRoom.increaseMemberCount();
        }
    }

    /**
     * 채팅방 권한 변경
     */
    public void updatePermission(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        // mastUserId == userId 인 경우만 권한 변경 가능
        validateMasterUser(userId, chatRoom.getMasterUserId(), ERROR_ONLY_MASTER_USER_CAN_CHANGE_PERMISSION);

        chatRoom.changePermission();
    }

    /**
     * 채팅방 이미지 변경
     */
    public void updateImageUrl(Long chatRoomId, String imageUrl) {
        ChatRoom chatRoom = findChatRoomById(chatRoomId);
        chatRoom.updateImageUrl(imageUrl);
    }

    /**
     * 현재 인기있는 채팅방 조회 : 같이 듣기 참가중인 참가자 수 상위 10개 채팅방 chatRoomDetails 반환
     */
    public ChatRoomDetailsListDto getTopChatRoomDetailsByUserCount(Long userId) {
        Set<ZSetOperations.TypedTuple<Object>> topChatRoomIds = playerSessionService.getTopChatRoomIdsByUserCountWithScores(ZSet_INDEX_START, ZSet_INDEX_END);
        return new ChatRoomDetailsListDto(topChatRoomIds.stream().map(tuple->{
            ChatRoom chatRoom = findChatRoomById(((Integer) Objects.requireNonNull(tuple.getValue())).longValue());
            return new ChatRoomDetailsDto(chatRoom, chatRoom.getIsMaster(userId));
        }).toList());
    }


    private ChatRoom findChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId).orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));
    }

    private User findUserById(Long userId) {
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
        if (!masterUserId.equals(userId)) {
            throw new NotMasterUserException(errorMessage);
        }
    }

    // 입장, 퇴장 시에 userChatRoom 상태 업데이트
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

    private ChatRoomMemberStatus updateChatRoomMemberStatusOnLeave(Long userId, ChatRoom chatRoom) {
        if (chatRoom.getMasterUserId().equals(userId)) {
            return ChatRoomMemberStatus.MASTER_LEAVING;
        }
        if (chatRoom.getCurrentMemberCount() <= 0) {
            return ChatRoomMemberStatus.EMPTY;
        }
        return ChatRoomMemberStatus.ACTIVE;
    }

    private ChatRoomMemberDto creatChatRoomMemberDto(User user, User findUser, Set<Long> friendsIdsFromUser) {
        return findUser.toUserChatRoomDto(validateUserRelationship(user, findUser, friendsIdsFromUser));
    }

    private Set<Long> getFriendsIdsFromUser(User user) {
        return new HashSet<>(friendRepository.findFriendsIdsFromUser(user));
    }

    private UserRelationship validateUserRelationship(User user, User findUser, Set<Long> friendsIdsFromUser) {
        if (user == findUser) {
            return UserRelationship.SELF;
        }
        if (friendsIdsFromUser.contains(findUser.getId())) {
            return UserRelationship.FRIEND;
        }
        return UserRelationship.NOT_FRIEND;
    }

    private ChatRoomDetailsDto createChatRoomDetailsDto(ChatRoom chatRoom, boolean isMaster) {
        return new ChatRoomDetailsDto(chatRoom, isMaster);
    }

    /**
     * 채팅방 삭제 시
     * 1. 채팅방 이미지 삭제
     * 2. 메시지 삭제
     * 3. 플레이리스트 삭제
     */
    public void deleteChatRoomData(Long chatRoomId, Long userId, Long masterUserId) {
        validateMasterUser(userId, masterUserId, ERROR_ONLY_MASTER_USER_CAN_DELETE_CHATROOM);
        chatRoomImageService.deleteImageById(chatRoomId);
        messageRepository.deleteByChatRoomId(chatRoomId);
        currentPlaylistRepository.deleteByChatRoomId(chatRoomId);
        chatRoomRepository.deleteById(chatRoomId);
    }
}