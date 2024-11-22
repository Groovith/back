package com.groovith.groovith.service.Image;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.groovith.groovith.domain.Image;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;


@RequiredArgsConstructor
@Service
public class S3Service {
    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * 파일 업로드 후 url 반환
     */
    public String uploadToS3AndGetUrl(String dir, MultipartFile multipartFile) {
        // 파일로 변환
        File file = convertMultiPartFileToFile(multipartFile);
        // 파일명 설정
        String fileName = generateFileName(dir, multipartFile);
        // s3에 업로드 후 url 반환
        String url = uploadFileToS3Bucket(fileName, file);
        file.delete();
        return url;
    }

    /**
     * S3에 파일 업로드
     */
    public String uploadFileToS3Bucket(String fileName, File file) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, file)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    /**
     * S3에 파일 삭제
     */
    public void deleteFileFromS3Bucket(String fileUrl, String dir) {
        // 파일 경로 추출
        String fileName = extractFileName(fileUrl);
        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        amazonS3Client.deleteObject(bucket, dir + fileName);
    }

    /**
     * MultipartFile -> File 로 변환
     */
    public File convertMultiPartFileToFile(MultipartFile file) {
        File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error converting multipart file to file", e);
        }
        return convertedFile;
    }


    public String generateFileName(String dir, MultipartFile multipartFile) {
        return dir + UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();
    }

    public String extractFileName(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }
}