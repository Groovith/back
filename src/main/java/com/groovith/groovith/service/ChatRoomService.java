package com.groovith.groovith.service;

import com.groovith.groovith.domain.*;
import com.groovith.groovith.dto.*;
import com.groovith.groovith.exception.ChatRoomFullException;
import com.groovith.groovith.exception.ChatRoomNotFoundException;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserChatRoomRepository userChatRoomRepository;
    private final CurrentPlaylistRepository currentPlaylistRepository;
    private final int MAX_MEMBER = 100;
    /**
     * 채팅방 생성
     * */
    public ChatRoom create(Long userId, CreateChatRoomRequestDto request){
        // 생성 실패해도 id 늘어남
        ChatRoom chatRoom = chatRoomRepository.save(request.toEntity());

        User user = userRepository.findById(userId)
                .orElseThrow(()->new UserNotFoundException(userId));

        // 유저 - 채팅방 연관관계 생성
        UserChatRoom.setUserChatRoom(user, chatRoom);

        // 채팅방 플레이리스트 생성
        currentPlaylistRepository.save(new CurrentPlaylist(chatRoom.getId()));

        return chatRoom;
    }

    /**
     * 채팅방 목록 조회
     * */
    @Transactional(readOnly = true)
    public List<ChatRoomListResponseDto> findAllDesc(){
        return chatRoomRepository.findAllDesc().stream()
                .map(ChatRoomListResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 내 채팅방 목록 조회
     */
    @Transactional(readOnly = true)
    public ChatRoomDetailsListDto getChatRoomsById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        // UserChatRoom 에서 현재 사용자가 참가 중인 채팅방 가져오기
        List<UserChatRoom> userChatRoomList = userChatRoomRepository.findByUserId(user.getId());

        // UserChatRoom 에서 ChatRoom 엔티티를 추출하려 List 로 변환
        List<ChatRoom> chatRoomList = userChatRoomList.stream().map(UserChatRoom::getChatRoom).toList();

        // ChatRoom 엔티티를 ChatRoomDetailsDto 로 변환 후 반환
        return new ChatRoomDetailsListDto(chatRoomList.stream().map(ChatRoomDetailsDto::new).toList());
    }


    /**
     * 채팅방 상세 조회
     * */
    @Transactional(readOnly = true)
    public ChatRoomDetailsDto findChatRoomDetail(Long chatRoomId){
        ChatRoom findChatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(()->new ChatRoomNotFoundException(chatRoomId));

        return new ChatRoomDetailsDto(findChatRoom);
    }

    /**
     *  채팅방 삭제
     * */
    public void deleteChatRoom(Long chatRoomId){
        chatRoomRepository.deleteById(chatRoomId);
    }


    /**
     * 채팅방 입장
     * */
    public void enterChatRoom(Long userId, Long chatRoomId){
        // 유저, 채팅방 조회
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(()->new ChatRoomNotFoundException(chatRoomId));
        // 유저가 이미 채팅방에 있는지 확인
        //있을경우 -> chatRoomId와 Redirect 메시지 전달 (클라이언트는 해당 id의 채팅방 페이지로 리디렉션)
        if (userChatRoomRepository.findByUserIdAndChatRoomId(userId, chatRoomId).isPresent()){
            //throw new IllegalArgumentException("채팅방에 이미 유저가 있습니다 userId:"+user.getId());
        }

        // 채팅방 인원 제한 : 100명
        if(chatRoom.getCurrentMemberCount() >= MAX_MEMBER){
            throw new ChatRoomFullException(chatRoomId);
        }
        // UserChatRoom 에 유저 등록
        UserChatRoom.setUserChatRoom(user, chatRoom);
        // currentMember += 1
        chatRoom.addUser();

    }



    /**
     *  채팅방 퇴장
     * */
    public void leaveChatRoom(Long userId, Long chatRoomId) {

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(()->new ChatRoomNotFoundException(chatRoomId));

        UserChatRoom userChatRoom = userChatRoomRepository.findByUserIdAndChatRoomId(user.getId(), chatRoomId)
                .orElseThrow(()->new IllegalArgumentException("채팅방에 유저가 존재하지 않음 userId:" + user.getId()));

        // 중간 테이블 삭제
        userChatRoomRepository.delete(userChatRoom);

        // User, ChatRoom의 연관관계 삭제
        UserChatRoom.deleteUserChatRoom(userChatRoom, user, chatRoom);
        // current 1 감소
        chatRoom.subUser();

        // 유저 퇴장시, 채팅방이 비어있다면 현재 채팅방 삭제
        if(chatRoom.getCurrentMemberCount()==0){
            chatRoomRepository.deleteById(chatRoomId);
            // 채팅방 플레이리스트 함께 삭제
            currentPlaylistRepository.deleteByChatRoomId(chatRoomId);
        }
    }


    @Transactional(readOnly = true)
    public List<UserChatRoomDto> findAllUser(Long chatRoomId){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(()->new ChatRoomNotFoundException(chatRoomId));

        return chatRoom.getUserChatRooms().stream()
                .map(userChatRoom -> userChatRoom.getUser().toUserChatRoomDto(userChatRoom.getUser()))
                .collect(Collectors.toList());
    }

    /**
     * 채팅방으로 초대
     */
    public void invite(Long inviterId, Long inviteeId, Long chatRoomId){
        log.info("invite service");
        // 초대받은 유저가 채팅방에 이미 참가중인지 확인
        if (userChatRoomRepository.findByUserIdAndChatRoomId(inviteeId, chatRoomId).isPresent()){
            throw new IllegalArgumentException("채팅방에 유저가 이미 존재합니다");
        }

        User inviter = userRepository.findById(inviterId)
                .orElseThrow(()->new UserNotFoundException(inviterId));
        User invitee = userRepository.findById(inviteeId)
                .orElseThrow(()-> new UserNotFoundException(inviteeId));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(()->new ChatRoomNotFoundException(chatRoomId));

        // 초대받은 유저와 채팅방 연관관계 생성
        UserChatRoom.setUserChatRoom(invitee, chatRoom);
        chatRoom.addUser();
    }


}