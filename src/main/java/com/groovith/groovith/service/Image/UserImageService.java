package com.groovith.groovith.service.Image;

import com.groovith.groovith.domain.Image;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.domain.enums.S3Directory;
import com.groovith.groovith.dto.DeleteProfilePictureResponseDto;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.repository.ImageRepository;
import com.groovith.groovith.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Qualifier("UserImageService")
@RequiredArgsConstructor
@Transactional
@Service
public class UserImageService implements ImageService {

    private final S3Service s3Service;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    @Override
    public String uploadAndSaveImage(MultipartFile file) {
        if (file == null) {
            return S3Directory.USER.getDefaultImageUrl();
        }
        String imageUrl = uploadImage(file);
        saveImage(imageUrl);
        return imageUrl;
    }

    @Override
    public String updateImageById(MultipartFile file, Long id) {
        // 기존 이미지 삭제
        deleteImageById(id);
        // 새 이미지 업로드
        return uploadAndSaveImage(file);
    }

    @Override
    public ResponseEntity<? super DeleteProfilePictureResponseDto> deleteImageById(Long userId) {
        try {
            User user = findUserById(userId);
            deleteImageIfNotDefault(user);
        }
        catch (Exception e) {
            return DeleteProfilePictureResponseDto.databaseError();
        }
        return DeleteProfilePictureResponseDto.success();
    }

    private String uploadImage(MultipartFile file) {
        return s3Service.uploadToS3AndGetUrl(file, S3Directory.USER.getDirectory());
    }

    private void deleteImageIfNotDefault(User user){
        if (!S3Directory.USER.isDefaultImage(user.getImageUrl())){
            s3Service.deleteFileFromS3Bucket(user.getImageUrl(), S3Directory.USER.getDefaultImageUrl());
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void saveImage(String imageUrl) {
        imageRepository.save(Image.builder()
                .imageUrl(imageUrl)
                .build());
    }
}
