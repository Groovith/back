package com.groovith.groovith.service;

import com.groovith.groovith.domain.FollowStatus;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.repository.FollowRepository;
import com.groovith.groovith.domain.Follow;
import com.groovith.groovith.dto.FollowResponse;
import com.groovith.groovith.repository.UserRepository;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.UserDetailsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowResponse getFollowing(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null; // 예외 추가 필요
        }

        List<UserDetailsResponseDto> followingList = new ArrayList<>();

        for(Follow follow : user.getFollowing()) {
            UserDetailsResponseDto response = new UserDetailsResponseDto();
            response.setUsername(follow.getFollowing().getUsername());
            followingList.add(response);
        }

        return new FollowResponse(followingList, null);
    }

    public FollowResponse getFollowers(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null; // 예외 추가 필요
        }

        List<UserDetailsResponseDto> followerList = new ArrayList<>();

        for(Follow follow : user.getFollowers()) {
            UserDetailsResponseDto response = new UserDetailsResponseDto();
            response.setUsername(follow.getFollowing().getUsername());
            followerList.add(response);
        }

        return new FollowResponse(null, followerList);
    }

    public ResponseEntity<?> follow(String followerUsername, String followingUsername) {
        User follower = userRepository.findByUsername(followerUsername).orElse(null);
        User following = userRepository.findByUsername(followingUsername).orElse(null);

        if (follower == null) {
            throw new IllegalArgumentException("Follower doesn't exist"); // 팔로워 없음 예외 발생
        }

        if (following == null) {
            throw new IllegalArgumentException("Following doesn't exist");
        }

        if (follower == following) {
            throw new IllegalArgumentException("Can't follow oneself");
        }

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new IllegalArgumentException("Already following");
        }

        Follow follow = new Follow();
        follow.setFollowing(following);
        follow.setFollower(follower);
        follow.setStatus(FollowStatus.PENDING); // 처음 상태는 보류중인 팔로우 요청
        followRepository.save(follow);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> unfollow(String followerUsername, String followingUsername) {
        User follower = userRepository.findByUsername(followerUsername).orElse(null);
        User following = userRepository.findByUsername(followingUsername).orElse(null);

        if (follower == null) {
            throw new IllegalArgumentException("Follower doesn't exist"); // 팔로워 없음 예외 발생
        }

        if (following == null) {
            throw new IllegalArgumentException("Following doesn't exist");
        }

        if (follower == following) {
            throw new IllegalArgumentException("Can't unfollow oneself");
        }

        if (!followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new IllegalArgumentException("Not following");
        }

        followRepository.deleteByFollowerAndFollowing(follower, following);

        return new ResponseEntity<>(HttpStatus.OK);
    }

//    // 수락하거나 거절하지 않은 팔로우 요청 조회
//    public ResponseEntity<?> findPendingFollows(Long userId){
//        User user = userRepository.findById(userId)
//                .orElseThrow(()-> new UserNotFoundException(userId));
//
//        List<UserDetailsResponseDto> followList = new ArrayList<>();
//
//        for(Follow follow : user.getFollowers()) {
//            UserDetailsResponseDto response = new UserDetailsResponseDto();
//            response.setUsername(follow.getFollowing().getUsername());
//            followerList.add(response);
//        }
//
//        return new FollowResponse(null, followerList);
//    }
}
