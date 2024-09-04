package com.groovith.groovith.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.groovith.groovith.domain.ChatRoom;
import com.groovith.groovith.domain.Image;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.DeleteProfilePictureResponseDto;
import com.groovith.groovith.exception.ChatRoomNotFoundException;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.repository.ChatRoomRepository;
import com.groovith.groovith.repository.ImageRepository;
import com.groovith.groovith.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ImageService {

    private final AmazonS3Client amazonS3Client;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    // 채팅방 이미지 저장 폴더
    private String CHATROOM_DIR = "chatroom/";
    // 유저 이미지 저장 폴더
    private String USER_DIR = "user/";

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.s3.defaultUserImageUrl}")
    private String DEFAULT_IMG_URL;

    /**
     *  파일 업로드, url db에 저장
     * */

    // 유저 이미지 업로드
    public void userUpLoadFile(MultipartFile multipartFile, Long userId){
        // 파일로 변환
        File file = convertMultiPartFileToFile(multipartFile);
        // 파일명 설정
        String fileName = USER_DIR + UUID.randomUUID()+ "_" + multipartFile.getOriginalFilename();
        // s3에 업로드 후 url 반환
        String url = uploadFileToS3Bucket(fileName, file);
        file.delete();

        // 이미지 url 저장
        Image image = Image.builder().imageUrl(url).build();

        // 유저 이미지 url 변경
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));
        user.setImageUrl(image.getImageUrl());

        imageRepository.save(image);
    }

    // 채팅방 이미지 업로드
    public void chatRoomUpLoadFile(MultipartFile multipartFile, Long chatRoomId){
        File file = convertMultiPartFileToFile(multipartFile);
        String fileName = CHATROOM_DIR+ UUID.randomUUID()+ "_" + multipartFile.getOriginalFilename();
        String url = uploadFileToS3Bucket(fileName, file);
        file.delete();

        Image image = Image.builder().imageUrl(url).build();

        // 채팅방 이미지 변경
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(()-> new ChatRoomNotFoundException(chatRoomId));
        chatRoom.setImageUrl(url);
        imageRepository.save(image);
    }

    // 삭제 후 유저 프로필 이미지를 기본 이미지로 변경하는 메서드
    public ResponseEntity<? super DeleteProfilePictureResponseDto> deleteProfilePicture(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));

            String currentImageUrl = user.getImageUrl();

            // 현재 이미지가 기본 이미지가 아닐 때 삭제
            if (!currentImageUrl.equals(DEFAULT_IMG_URL)) {
                deleteFileFromS3Bucket(currentImageUrl);
            }

            // 유저 이미지 URL을 기본 이미지로 설정
            user.setImageUrl(DEFAULT_IMG_URL);
            userRepository.save(user); // User 정보를 업데이트
        } catch (Exception e) {
            return DeleteProfilePictureResponseDto.databaseError();
        }
        return DeleteProfilePictureResponseDto.success();
    }

    // S3에서 파일 삭제
    private void deleteFileFromS3Bucket(String fileUrl) {
        // 파일 경로 추출
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, USER_DIR + fileName));
    }

    /**
     * S3에 파일 업로드
     * */
    private String uploadFileToS3Bucket(String fileName, File file) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    /**
     * MultipartFile -> File 로 변환
     * */
    private File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error converting multipart file to file", e);
        }
        return convertedFile;
    }

}
