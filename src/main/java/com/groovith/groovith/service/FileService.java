package com.groovith.groovith.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.groovith.groovith.domain.FileEntity;
import com.groovith.groovith.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class FileService {

    private final AmazonS3Client amazonS3Client;
    private final FileRepository fileRepository;


    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     *  파일 업로드, url db에 저장
     * */
    public FileEntity uploadFile(MultipartFile multipartFile) {
        File file = convertMultiPartFileToFile(multipartFile);
        String fileName = multipartFile.getOriginalFilename();
        String url = uploadFileToS3Bucket(fileName, file);
        file.delete();

        FileEntity fileEntity = FileEntity.builder().imageUrl(url).build();
        return fileRepository.save(fileEntity);
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
