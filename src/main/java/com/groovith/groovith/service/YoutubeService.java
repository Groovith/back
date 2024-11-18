package com.groovith.groovith.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.groovith.groovith.domain.Track;
import com.groovith.groovith.dto.TrackDto;
import com.groovith.groovith.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;

@Transactional
@RequiredArgsConstructor
@Service
public class YoutubeService {

    private static final String TOPIC = " - Topic";

    private final YouTube youtube;
    @Value("${youtube.apikey}")
    private String apiKey;
    private final TrackRepository trackRepository;


    /**
     * Youtube Video 정보 가져오기
     * - snippet : 제목, 설명, 썸네일 이미지 등
     * - contentDetails: duration(ISO 8601 형식)
     * */
    public TrackDto getVideo(String videoId) throws IOException {
        YouTube.Videos.List data = youtube.videos().list(Collections.singletonList("snippet,contentDetails"));
        // videoId 지정
        data.setId(Collections.singletonList(videoId));
        // apikey 지정
        data.setKey(apiKey);
        Video video = data.execute().getItems().get(0);
        // 트랙 저장
        TrackDto trackDto = createTrackDto(video);
        trackRepository.save(new Track(trackDto));

        // 비디오 리스트를 VideoDto로 변환
        return trackDto;
    }


    private TrackDto createTrackDto(Video video){
        // 제목, 가수 명, 길이, 이미지 url 가져오기
        TrackDto trackDto = new TrackDto();
        trackDto.setVideoId(video.getId());
        trackDto.setTitle(video.getSnippet().getTitle());
        trackDto.setArtist(parsingArtist(video.getSnippet().getChannelTitle()));   // 일단 채널 명
        trackDto.setImageUrl(video.getSnippet().getThumbnails().getDefault().getUrl());
        trackDto.setDuration(Duration.parse(video.getContentDetails().getDuration()).toSeconds());
        return trackDto;
    }


    private String parsingArtist(String title){
        if (title.endsWith(TOPIC)) {
            return title.substring(0, title.length() - TOPIC.length());
        }
        return title;
    }
}