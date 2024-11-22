package com.groovith.groovith.service.Image;

import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.Image;
import com.groovith.groovith.domain.enums.S3Directory;
import com.groovith.groovith.exception.ChatRoomNotFoundException;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.ImageRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Qualifier("ChatRoomImageService")
@Transactional
@Service
public class ChatRoomImageService extends AbstractImageService<ChatRoom> {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomImageService(S3Service s3Service, ImageRepository imageRepository, ChatRoomRepository chatRoomRepository) {
        super(s3Service, imageRepository);
        this.chatRoomRepository = chatRoomRepository;
    }

    @Override
    protected String getDefaultImageUrl() {
        return S3Directory.CHATROOM.getDefaultImageUrl();
    }

    @Override
    protected String getDirectory() {
        return S3Directory.USER.getDirectory();
    }

    @Override
    protected String uploadImage(MultipartFile file) {
        return s3Service.uploadToS3AndGetUrl(file, this.getDirectory());
    }

    @Override
    protected void deleteImageIfNotDefault(ChatRoom chatRoom) {
        if(!S3Directory.CHATROOM.isDefaultImage(chatRoom.getImageUrl())) {
            s3Service.deleteFileFromS3Bucket(chatRoom.getImageUrl(), this.getDirectory());
        }
    }

    @Override
    protected ChatRoom findEntityById(Long id) {
        return chatRoomRepository.findById(id).orElseThrow(() -> new ChatRoomNotFoundException(id));
    }

    @Override
    protected void saveImage(String imageUrl) {
        imageRepository.save(Image.builder()
                .imageUrl(imageUrl)
                .build());
    }
}
