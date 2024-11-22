package com.groovith.groovith.service.Image;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.Image;
import com.groovith.groovith.domain.enums.S3Directory;
import com.groovith.groovith.dto.DeleteProfilePictureResponseDto;
import com.groovith.groovith.exception.ChatRoomNotFoundException;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.ImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.*;

@Qualifier("ChatRoomImageService")
@RequiredArgsConstructor
@Transactional
@Service
public class ChatRoomImageService implements ImageService {

    private final S3Service s3Service;
    private final ImageRepository imageRepository;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 채팅방 생성시 사용: 이미지 s3에 업로드 후 저장
     * */
    @Override
    public String uploadAndSaveImage(MultipartFile file) {
        if (file == null) {
            return S3Directory.CHATROOM.getDefaultImageUrl();
        }
        String imageUrl = uploadImage(file);
        saveImage(imageUrl);
        return imageUrl;
    }

    /**
     * 채팅방 수정 시 사용: 기존 이미지 제거 후 새 이미지 업로드
     * */
    @Override
    public String updateImageById(MultipartFile file, Long id) {
        deleteImageById(id);
        return uploadAndSaveImage(file);
    }

    @Override
    public ResponseEntity<? super DeleteProfilePictureResponseDto> deleteImageById(Long id) {
        try {
            ChatRoom chatRoom = findChatRoomById(id);
            deleteImageIfNotDefault(chatRoom);
        } catch (Exception e) {
            return DeleteProfilePictureResponseDto.databaseError();
        }
        return DeleteProfilePictureResponseDto.success();
    }

    private String uploadImage(MultipartFile file){
        return s3Service.uploadToS3AndGetUrl(file, S3Directory.CHATROOM.getDirectory());
    }

    private void deleteImageIfNotDefault(ChatRoom chatRoom) {
        if (!S3Directory.CHATROOM.isDefaultImage(chatRoom.getImageUrl())) {
            s3Service.deleteFileFromS3Bucket(chatRoom.getImageUrl(), S3Directory.USER.getDefaultImageUrl());
        }
    }

    private ChatRoom findChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatRoomNotFoundException(chatRoomId));
    }

    private void saveImage(String imageUrl) {
        imageRepository.save(Image.builder()
                .imageUrl(imageUrl)
                .build());
    }

}
