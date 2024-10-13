package com.groovith.groovith.service;

import com.groovith.groovith.domain.Friend;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.FriendListResponseDto;
import com.groovith.groovith.dto.UserDetailsResponseDto;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.repository.FriendRepository;
import com.groovith.groovith.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;



@RequiredArgsConstructor
@Transactional
@Service
public class FriendService {
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    /**
     * 친구 만들기(from_user 의 친구 목록에 to_user 추가)
     * */
    public void addFriend(Long from_id, String toUserName){
        // 친구 추가한 유저
        User fromUser = userRepository.findById(from_id)
                .orElseThrow(()-> new UserNotFoundException(from_id));
        // 친구 추가된 유저
        User toUser = userRepository.findByUsername(toUserName)
                .orElseThrow(()-> new UserNotFoundException(toUserName));

        if(friendRepository.existsByFromUserAndToUser(fromUser, toUser)){
           throw new IllegalArgumentException("이미 친구관계입니다. from:"+from_id+" to:"+toUserName);
        }

        Friend friend = Friend.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .build();

        friendRepository.save(friend);
    }

    /**
     * 친구 제거하기
     * */
    public void subFriend(Long from_id, String toUserName){
        System.out.println("username : "+ toUserName);
        User fromUser = userRepository.findById(from_id)
                .orElseThrow(()-> new UserNotFoundException(from_id));
        User toUser = userRepository.findByUsername(toUserName)
                .orElseThrow(()-> new UserNotFoundException(toUserName));

        Optional<Friend> friend = friendRepository.findByFromUserAndToUser(fromUser, toUser);
        if(!friend.isPresent()){
            throw new IllegalArgumentException("친구 관계가 아닙니다. from:"+from_id+" to:"+toUserName);
        }


        friendRepository.deleteById(friend.get().getId());
    }

    /**
     * 친구 목록 불러오기
     * */
    @Transactional(readOnly = true)
    public FriendListResponseDto getFriends(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));
        List<User> friends = user.getFriends().stream().map(Friend::getToUser).toList();
        return new FriendListResponseDto(friends.stream().map(UserDetailsResponseDto::new).toList());
    }
}
