package com.groovith.groovith.controller;

import com.groovith.groovith.dto.TrackDto;
import com.groovith.groovith.dto.VideoRequestDto;
import com.groovith.groovith.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class YoutubeController {
    private final YoutubeService youtubeService;

    @GetMapping("/video")
    public ResponseEntity<TrackDto> getVideo(@RequestParam String videoId) throws IOException {
        TrackDto trackDto = youtubeService.getVideo(videoId);
        return new ResponseEntity<>(trackDto, HttpStatus.OK);
    }

}
