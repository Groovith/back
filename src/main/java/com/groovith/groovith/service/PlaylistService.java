package com.groovith.groovith.service;

import com.groovith.groovith.domain.CurrentPlaylist;
import com.groovith.groovith.domain.CurrentPlaylistTrack;
import com.groovith.groovith.repository.CurrentPlaylistRepository;
import com.groovith.groovith.repository.CurrentPlaylistTrackRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaylistService {
    private final CurrentPlaylistRepository currentPlaylistRepository;
    private final CurrentPlaylistTrackRepository currentPlaylistTrackRepository;

    @Transactional
    public void deleteTrackByIndex(Long chatRoomId, int index) {
        CurrentPlaylist currentPlaylist = currentPlaylistRepository.findByChatRoomId(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("Playlist with chatRoomId: " + chatRoomId + "is not found."));

        if (isIndexOutOfBounds(index, currentPlaylist.getCurrentPlaylistTracks().size())) {
            throw new IndexOutOfBoundsException("Invalid track index: " + index + "in Range: " + (currentPlaylist.getCurrentPlaylistTracks().size() - 1));
        }

        CurrentPlaylistTrack currentPlaylistTrack = currentPlaylist.getCurrentPlaylistTracks().get(index);

        currentPlaylist.getCurrentPlaylistTracks().remove(index);
        currentPlaylistTrackRepository.delete(currentPlaylistTrack);
    }

    @Transactional
    public void savePlayListByChatRoomId(Long chatRoomId){
        currentPlaylistRepository.save(new CurrentPlaylist(chatRoomId));
    }

    @Transactional
    public void deletePlayListByChatRoomId(Long chatRoomId) {
        currentPlaylistRepository.deleteByChatRoomId(chatRoomId);
    }
    private boolean isIndexOutOfBounds(int index, int size) {
        return index < 0 || index >= size;
    }
}
