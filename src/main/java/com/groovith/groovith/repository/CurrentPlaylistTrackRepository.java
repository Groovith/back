package com.groovith.groovith.repository;

import com.groovith.groovith.domain.CurrentPlaylist;
import com.groovith.groovith.domain.CurrentPlaylistTrack;
import com.groovith.groovith.domain.Track;
import com.groovith.groovith.dto.TrackDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrentPlaylistTrackRepository extends JpaRepository<CurrentPlaylistTrack, Long> {
    CurrentPlaylistTrack findByCurrentPlaylistAndTrack(CurrentPlaylist currentPlaylist, Track track);
}
