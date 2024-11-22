package com.groovith.groovith.service.Image;

import com.groovith.groovith.domain.Image;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.domain.enums.S3Directory;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.repository.ImageRepository;
import com.groovith.groovith.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Qualifier("UserImageService")
@Transactional
@Service
public class UserImageService extends AbstractImageService<User> {

    private final UserRepository userRepository;

    public UserImageService(S3Service s3Service, ImageRepository imageRepository, UserRepository userRepository) {
        super(s3Service, imageRepository);
        this.userRepository = userRepository;
    }

    @Override
    protected String getDefaultImageUrl() {
        return S3Directory.USER.getDefaultImageUrl();
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
    protected void deleteImageIfNotDefault(User user) {
        if(!S3Directory.USER.isDefaultImage(user.getImageUrl())){
            s3Service.deleteFileFromS3Bucket(user.getImageUrl(), this.getDirectory());
        }
    }

    @Override
    protected User findEntityById(Long id) {
        return userRepository.findById(id).orElseThrow(()->new UserNotFoundException(id));
    }

    @Override
    protected void saveImage(String imageUrl) {
        imageRepository.save(Image.builder()
                .imageUrl(imageUrl)
                .build());
    }
}
