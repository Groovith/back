package com.groovith.groovith.dto;

import lombok.Data;

//영상 제목, 아티스트, 이미지url, 영상 길이

@Data
public class VideoDto {
    private String title;
    private String artist;
    private String imageUrl;
    private Long Duration;
}
