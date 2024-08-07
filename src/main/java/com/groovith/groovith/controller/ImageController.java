package com.groovith.groovith.controller;

import com.groovith.groovith.domain.Image;
import com.groovith.groovith.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/files/upload")
    public ResponseEntity<Image> uploadFile(@RequestParam("file") MultipartFile file) {
        Image fileEntity = imageService.uploadFile(file);
        return new ResponseEntity<>(fileEntity, HttpStatus.OK);
    }


}
