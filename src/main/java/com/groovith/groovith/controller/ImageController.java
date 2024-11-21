package com.groovith.groovith.controller;

import com.groovith.groovith.dto.DeleteProfilePictureResponseDto;
import com.groovith.groovith.security.CustomUserDetails;
import com.groovith.groovith.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * 유저 이미지 업로드(프로필 사진 수정, 교체)
     * */
    @PutMapping("/upload/user")
    public ResponseEntity<?> userUploadFile(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam("file") MultipartFile file) {
        imageService.userUpLoadFile(file, userDetails.getUserId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/users/me/update/profile-picture")
    public ResponseEntity<? super DeleteProfilePictureResponseDto> deleteProfilePicture(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return imageService.deleteProfilePicture(userDetails.getUserId());
    }

    /**
     * 채팅방 이미지 업로드(수정, 교체)
     * */
    @PutMapping("/upload/chatroom/{chatRoomId}")
    public ResponseEntity<?> chatRoomUploadFile(@RequestParam("file") MultipartFile file, @PathVariable("chatRoomId")Long chatRoomId) {
        imageService.chatRoomUpLoadFile(file, chatRoomId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
