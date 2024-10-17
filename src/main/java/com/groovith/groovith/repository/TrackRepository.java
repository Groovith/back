package com.groovith.groovith.repository;

import com.groovith.groovith.domain.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackRepository extends JpaRepository<Track, String> {
    boolean existsByVideoId(String id);
}
