package com.groovith.groovith.dto;

import com.groovith.groovith.domain.Track;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//영상 제목, 아티스트, 이미지url, 영상 길이

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TrackDto {
    private String videoId;
    private String title;
    private String artist;
    private String imageUrl;
    private Long duration;
    public TrackDto(Track track){
        this.videoId = track.getVideoId();
        this.title = track.getTitle();
        this.artist = track.getArtist();
        this.imageUrl = track.getImageUrl();
        this.duration = track.getDuration();
    }
}
