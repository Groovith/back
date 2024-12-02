package com.groovith.groovith.repository;

import com.groovith.groovith.domain.CurrentPlaylist;
import com.groovith.groovith.domain.CurrentPlaylistTrack;
import com.groovith.groovith.domain.Track;
import com.groovith.groovith.dto.TrackDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrentPlaylistTrackRepository extends JpaRepository<CurrentPlaylistTrack, Long> {
    CurrentPlaylistTrack findByCurrentPlaylistAndTrack(CurrentPlaylist currentPlaylist, Track track);

    @Query("SELECT t FROM CurrentPlaylistTrack cpt " +
            "JOIN cpt.track t " +
            "WHERE cpt.currentPlaylist.chatRoomId = :chatRoomId")
    List<Track> findTrackListByChatRoomId(Long chatRoomId);
}
