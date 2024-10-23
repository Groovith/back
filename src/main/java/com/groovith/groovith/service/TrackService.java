package com.groovith.groovith.service;

import com.groovith.groovith.domain.Track;
import com.groovith.groovith.dto.TrackDto;
import com.groovith.groovith.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TrackService {
    private final TrackRepository trackRepository;

    public void save(TrackDto trackDto){
        if(!trackRepository.existsByVideoId(trackDto.getVideoId())){
            trackRepository.save(new Track(trackDto));
        }
    }

}
