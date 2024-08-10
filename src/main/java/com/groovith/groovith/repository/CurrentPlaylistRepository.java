package com.groovith.groovith.repository;

import com.groovith.groovith.domain.CurrentPlaylist;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CurrentPlaylistRepository extends MongoRepository<CurrentPlaylist, String> {
    Optional<CurrentPlaylist> findByChatRoomId(Long chatRoomId);
    void deleteByChatRoomId(Long chatRoomId);
}
