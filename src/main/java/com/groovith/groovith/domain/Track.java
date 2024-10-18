package com.groovith.groovith.domain;

import com.groovith.groovith.dto.TrackDto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Entity
public class Track {
    @Id
    @Column(nullable = false)
    private String videoId;
    private String title;
    private String artist;
    private String imageUrl;
    private Long duration;

    @OneToMany(mappedBy = "track")
    private List<CurrentPlaylistTrack> currentPlaylistTracks;

    public Track(TrackDto trackDto){
        this.videoId = trackDto.getVideoId();
        this.title = trackDto.getTitle();
        this.artist = trackDto.getArtist();
        this.imageUrl = trackDto.getImageUrl();
        this.duration = trackDto.getDuration();
    }

    public static TrackDto toDto(Track track){
        return new TrackDto(track);
    }
}
