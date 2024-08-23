package com.groovith.groovith.service;

import com.groovith.groovith.domain.FollowStatus;
import com.groovith.groovith.domain.UserStatus;
import com.groovith.groovith.dto.PendingFollowsResponseDto;
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

import javax.tools.ForwardingFileObject;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;


    /**
     * 팔로우 요청 생성
     * */
    @Transactional
    public ResponseEntity<?> follow(String followerUsername, String followingUsername) {
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(()->new UserNotFoundException(followerUsername));
        User following = userRepository.findByUsername(followingUsername)
                .orElseThrow(()->new UserNotFoundException(followingUsername));

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
        if(following.getStatus() == UserStatus.PRIVATE) {
            follow.setStatus(FollowStatus.PENDING); // 비공개일 경우에는 요청 보류
        } else if (following.getStatus() == UserStatus.PUBLIC) {
            follow.setStatus(FollowStatus.ACCEPTED); // 공개일 경우에는 바로 ACCEPTED
        }
        followRepository.save(follow);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 언팔로우
     * */
    @Transactional
    public ResponseEntity<?> unfollow(String followerUsername, String followingUsername) {
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(()-> new UserNotFoundException(followerUsername));
        User following = userRepository.findByUsername(followingUsername)
                .orElseThrow(()-> new UserNotFoundException(followingUsername));

        if (follower == null) {
            throw new IllegalArgumentException("Follower doesn't exist"); // 팔로워 없음 예외 발생
        }

        if (following == null) {
            throw new IllegalArgumentException("Following doesn't exist");
        }

        if (follower == following) {
            throw new IllegalArgumentException("Can't unfollow oneself");
        }

//        if (!followRepository.existsByFollowerAndFollowing(follower, following)) {
//            throw new IllegalArgumentException("Not following");
//        }
        Follow follow = followRepository.findByFollowerIdAndFollowingId(follower.getId(), following.getId())
                .orElseThrow(()-> new IllegalArgumentException("팔로우 요청이 존재하지 않습니다."));
        if(follow.getStatus()!=FollowStatus.ACCEPTED){
            throw new IllegalArgumentException("승인된 팔로우 관계가 아닙니다.");
        }
        followRepository.deleteById(follow.getId());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 팔로우 수락(비공계 계정에 팔로우 요청한 경우)
     * */
    @Transactional
    public void acceptFollow(String followerUsername, String followingUsername){
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(()->new UserNotFoundException(followerUsername));
        User following = userRepository.findByUsername(followingUsername)
                .orElseThrow(()->new UserNotFoundException(followingUsername));

        Follow follow = followRepository.findByFollowerIdAndFollowingId(follower.getId(), following.getId())
                .orElseThrow(()-> new IllegalArgumentException("팔로우 요청이 존재하지 않습니다."));
        if(follow.getStatus()==FollowStatus.REJECTED){
            throw new IllegalArgumentException("이미 거부당한 팔로우 요청입니다.");
        } else if(follow.getStatus()==FollowStatus.ACCEPTED){
            throw new IllegalArgumentException("이미 승인한 팔로우 요청입니다.");
        }
        follow.updateStatus(FollowStatus.ACCEPTED); // 팔로우 요청을 승인
    }

    /**
     * 팔로우 거부(비공계 계정에 팔로우 요청한 경우)
     * */
    @Transactional
    public void rejectFollow(String followerUsername, String followingUsername){
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(()->new UserNotFoundException(followerUsername));
        User following = userRepository.findByUsername(followingUsername)
                .orElseThrow(()->new UserNotFoundException(followingUsername));

        Follow follow = followRepository.findByFollowerIdAndFollowingId(follower.getId(), following.getId())
                .orElseThrow(()-> new IllegalArgumentException("팔로우 요청이 존재하지 않습니다."));
        if(follow.getStatus()==FollowStatus.REJECTED){
            throw new IllegalArgumentException("이미 거부당한 팔로우 요청입니다.");
        } else if(follow.getStatus()==FollowStatus.ACCEPTED){
            throw new IllegalArgumentException("이미 승인한 팔로우 요청입니다.");
        }
        follow.updateStatus(FollowStatus.REJECTED); // 팔로우 요청을 승인
    }

    /**
     * 팔로잉 목록 조회
     * */
    public FollowResponse getFollowing(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null; // 예외 추가 필요
        }

        List<UserDetailsResponseDto> followingList = new ArrayList<>();

        for(Follow follow : user.getFollowing()) {
            // 팔로우 요청을 받은 유저들만 반환
            if(follow.getStatus()==FollowStatus.ACCEPTED){
                UserDetailsResponseDto response = new UserDetailsResponseDto();
                response.setUsername(follow.getFollowing().getUsername());
                followingList.add(response);
            }
        }

        return new FollowResponse(followingList, null);
    }

    /**
     * 팔로우 목록 조회
     */
    public FollowResponse getFollowers(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null; // 예외 추가 필요
        }

        List<UserDetailsResponseDto> followerList = new ArrayList<>();

        for(Follow follow : user.getFollowers()) {
            if(follow.getStatus()==FollowStatus.ACCEPTED){
                UserDetailsResponseDto response = new UserDetailsResponseDto();
                response.setUsername(follow.getFollowing().getUsername());
                followerList.add(response);
            }
        }

        return new FollowResponse(null, followerList);
    }





    // 보류중인 팔로우 요청 조회()
    public List<PendingFollowsResponseDto> findPendingFollows(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));

        List<PendingFollowsResponseDto> followList = new ArrayList<>();

        for(Follow follow : user.getFollowers()) {
            if(follow.getStatus()==FollowStatus.PENDING){
                PendingFollowsResponseDto dto = new PendingFollowsResponseDto();
                dto.setFollowerName(follow.getFollower().getUsername());
                followList.add(dto);
            }
        }
        return followList;
    }
}
