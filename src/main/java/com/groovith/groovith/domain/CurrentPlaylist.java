package com.groovith.groovith.domain;

import com.groovith.groovith.dto.SpotifyTrackDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.List;



@Data
@NoArgsConstructor
@Entity
public class CurrentPlaylist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "current_playlist_id")
    private Long id;

    private Long chatRoomId;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<SpotifyTrackDto> tracks;

    @Builder
    public CurrentPlaylist(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
        this.tracks = new ArrayList<>();
    }
}
