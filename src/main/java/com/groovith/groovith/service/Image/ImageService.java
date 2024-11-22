package com.groovith.groovith.service.Image;

import com.groovith.groovith.dto.DeleteProfilePictureResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;


public interface ImageService {

    String uploadAndSaveImage(MultipartFile file);
    String updateImageById(MultipartFile file, Long id);
    ResponseEntity<? super DeleteProfilePictureResponseDto> deleteImageById(Long id);

}
