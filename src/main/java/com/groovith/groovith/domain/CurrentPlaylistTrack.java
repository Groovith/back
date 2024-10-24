package com.groovith.groovith.domain;

import com.groovith.groovith.dto.TrackDto;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class CurrentPlaylistTrack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currentPlaylist_id")
    private CurrentPlaylist currentPlaylist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="track_id")
    private Track track;

    @Builder
    public CurrentPlaylistTrack(CurrentPlaylist currentPlaylist, Track track){
        this.currentPlaylist = currentPlaylist;
        this.track = track;
    }

    public static CurrentPlaylistTrack setPlaylistTrack(CurrentPlaylist currentPlaylist, Track track){
        CurrentPlaylistTrack currentPlaylistTrack = CurrentPlaylistTrack.builder()
                .currentPlaylist(currentPlaylist)
                .track(track)
                .build();
        currentPlaylist.getCurrentPlaylistTracks().add(currentPlaylistTrack);
        track.getCurrentPlaylistTracks().add(currentPlaylistTrack);
        return currentPlaylistTrack;
    }
}
