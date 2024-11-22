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

@Qualifier("ChatRoomImageService")
@RequiredArgsConstructor
@Transactional
@Service
public class ChatRoomImageService implements ImageService {

    private final S3Service s3Service;
    private final ImageRepository imageRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public String uploadAndSaveImage(MultipartFile multipartFile) {
        if (multipartFile == null) {
            return S3Directory.CHATROOM.getDefaultImageUrl();
        }
        String imageUrl = s3Service.uploadToS3AndGetUrl(S3Directory.CHATROOM.getDirectory(), multipartFile);
        saveImage(imageUrl);
        return imageUrl;
    }

    @Override
    public ResponseEntity<? super DeleteProfilePictureResponseDto> deleteImageById(Long chatRoomId) {
        try {
            ChatRoom chatRoom = findChatRoomById(chatRoomId);
            deleteImageIfNotDefault(chatRoom);
        }
        catch (Exception e) {
            return DeleteProfilePictureResponseDto.databaseError();
        }
        return DeleteProfilePictureResponseDto.success();
    }

    private void deleteImageIfNotDefault(ChatRoom chatRoom){
        if (!S3Directory.CHATROOM.isDefaultImage(chatRoom.getImageUrl())){
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
