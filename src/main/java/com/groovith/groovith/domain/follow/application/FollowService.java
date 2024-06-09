package com.groovith.groovith.domain.follow.application;

import com.groovith.groovith.domain.follow.dao.FollowRepository;
import com.groovith.groovith.domain.follow.domain.FollowEntity;
import com.groovith.groovith.domain.follow.dto.FollowResponse;
import com.groovith.groovith.domain.user.dao.UserRepository;
import com.groovith.groovith.domain.user.domain.UserEntity;
import com.groovith.groovith.domain.user.dto.UserDetailsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowResponse getFollowing(String username) {
        UserEntity userEntity = userRepository.findByUsername(username).orElse(null);
        if (userEntity == null) {
            return null; // 예외 추가 필요
        }

        List<UserDetailsResponse> followingList = new ArrayList<>();

        for(FollowEntity followEntity : userEntity.getFollowing()) {
            UserDetailsResponse response = new UserDetailsResponse();
            response.setUsername(followEntity.getFollowing().getUsername());
            followingList.add(response);
        }

        return new FollowResponse(followingList, null);
    }

    public FollowResponse getFollowers(String username) {
        UserEntity userEntity = userRepository.findByUsername(username).orElse(null);
        if (userEntity == null) {
            return null; // 예외 추가 필요
        }

        List<UserDetailsResponse> followerList = new ArrayList<>();

        for(FollowEntity followEntity : userEntity.getFollowers()) {
            UserDetailsResponse response = new UserDetailsResponse();
            response.setUsername(followEntity.getFollowing().getUsername());
            followerList.add(response);
        }

        return new FollowResponse(null, followerList);
    }

    public ResponseEntity<?> follow(String followerUsername, String followingUsername) {
        UserEntity follower = userRepository.findByUsername(followerUsername).orElse(null);
        UserEntity following = userRepository.findByUsername(followingUsername).orElse(null);

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

        FollowEntity follow = new FollowEntity();
        follow.setFollowing(following);
        follow.setFollower(follower);
        followRepository.save(follow);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> unfollow(String followerUsername, String followingUsername) {
        UserEntity follower = userRepository.findByUsername(followerUsername).orElse(null);
        UserEntity following = userRepository.findByUsername(followingUsername).orElse(null);

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
}
