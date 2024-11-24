package com.groovith.groovith.service.Image;

import com.groovith.groovith.dto.DeleteProfilePictureResponseDto;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.ImageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;


public abstract class AbstractImageService<T> implements ImageService {

    protected final S3Service s3Service;
    protected final ImageRepository imageRepository;

    protected AbstractImageService(S3Service s3Service, ImageRepository imageRepository) {
        this.s3Service = s3Service;
        this.imageRepository = imageRepository;
    }

    /**
     *  이미지 s3에 업로드 url 반환 (채팅방 생성 시 사용)
     * */
    @Override
    public String uploadAndSaveImage(MultipartFile file) {
        if (file == null) {
            return getDefaultImageUrl();
        }
        String imageUrl = uploadImage(file);
        saveImage(imageUrl);
        return imageUrl;
    }

    /**
     * 기존 이미지 제거 후 새 이미지 업로드
     * */
    @Override
    public String updateImageById(MultipartFile file, Long id) {
        // 파일이 비어있을 경우
        if (isFileEmpty(file)) {
            return null;
        }
        deleteImageById(id);
        return uploadAndSaveImage(file);
    }

    /**
     * 기존 이미지 삭제
     * */
    @Override
    public ResponseEntity<? super DeleteProfilePictureResponseDto> deleteImageById(Long id) {
        try {
            T entity = findEntityById(id);
            deleteImageIfNotDefault(entity);
        } catch (Exception e) {
            return DeleteProfilePictureResponseDto.databaseError();
        }
        return DeleteProfilePictureResponseDto.success();
    }

    private boolean isFileEmpty(MultipartFile file) {
        return file == null || file.isEmpty();
    }

    protected abstract String getDefaultImageUrl();

    protected abstract String getDirectory();

    protected abstract String uploadImage(MultipartFile file);

    protected abstract void deleteImageIfNotDefault(T entity);

    protected abstract T findEntityById(Long id);

    protected abstract void saveImage(String imageUrl);
}
