package com.groovith.groovith.controller;

import com.groovith.groovith.dto.FriendListResponseDto;
import com.groovith.groovith.dto.FriendRequestDto;
import com.groovith.groovith.security.CustomUserDetails;
import com.groovith.groovith.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class FriendController {
    private final FriendService friendService;

    /**
     *  친구 추가하기
     * */
    @PostMapping("/friend")
    public ResponseEntity<?> addFriend(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody FriendRequestDto friendRequestDto){
        friendService.addFriend(userDetails.getUserId(), friendRequestDto.getToUser());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     *  친구 삭제하기
     * */
    @DeleteMapping("/friend")
    public ResponseEntity<?> subFriend(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody FriendRequestDto friendRequestDto){
        friendService.subFriend(userDetails.getUserId(), friendRequestDto.getToUser());
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     *  친구 목록 불러오기
     * */
    @GetMapping("/users/me/friends")
    public ResponseEntity<FriendListResponseDto> getFriends(@AuthenticationPrincipal CustomUserDetails userDetails){
        return new ResponseEntity<>(friendService.getFriends(userDetails.getUserId()), HttpStatus.OK);
    }
}
