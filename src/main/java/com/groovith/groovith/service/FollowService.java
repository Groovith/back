package com.groovith.groovith.service;

import com.groovith.groovith.repository.FollowRepository;
import com.groovith.groovith.domain.Follow;
import com.groovith.groovith.dto.FollowResponse;
import com.groovith.groovith.repository.UserRepository;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.UserDetailsResponse;
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
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return null; // 예외 추가 필요
        }

        List<UserDetailsResponse> followingList = new ArrayList<>();

        for(Follow follow : user.getFollowing()) {
            UserDetailsResponse response = new UserDetailsResponse();
            response.setUsername(follow.getFollowing().getUsername());
            followingList.add(response);
        }

        return new FollowResponse(followingList, null);
    }

    public FollowResponse getFollowers(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return null; // 예외 추가 필요
        }

        List<UserDetailsResponse> followerList = new ArrayList<>();

        for(Follow follow : user.getFollowers()) {
            UserDetailsResponse response = new UserDetailsResponse();
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
}
