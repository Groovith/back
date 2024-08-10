package com.groovith.groovith.domain;

import com.groovith.groovith.dto.SpotifyTrackDto;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "currentPlaylist")
@Data
public class CurrentPlaylist {
    @Id
    private String _id;
    private Long chatRoomId;
    private List<SpotifyTrackDto> tracks;

    @Builder
    public CurrentPlaylist(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
        this.tracks = new ArrayList<>();
    }
}
