package com.groovith.groovith.domain;

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
    private List<String> videoList = new ArrayList<>();

    @Builder
    public CurrentPlaylist(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
        this.videoList = new ArrayList<>();
    }
}
