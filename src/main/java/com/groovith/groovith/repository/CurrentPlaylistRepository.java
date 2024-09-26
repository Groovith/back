package com.groovith.groovith.repository;

import com.groovith.groovith.domain.CurrentPlaylist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CurrentPlaylistRepository extends JpaRepository<CurrentPlaylist, String> {
    Optional<CurrentPlaylist> findByChatRoomId(Long chatRoomId);
    void deleteByChatRoomId(Long chatRoomId);
}
