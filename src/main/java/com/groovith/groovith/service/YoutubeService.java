package com.groovith.groovith.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.groovith.groovith.domain.Track;
import com.groovith.groovith.dto.TrackDto;
import com.groovith.groovith.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
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

    private  final YouTube youtube;
    @Value("${youtube.apikey}")
    private String apiKey;
    private final TrackRepository trackRepository;


    /**
     * Youtube Video 정보 가져오기
     * */
    @Transactional(readOnly = true)
    public TrackDto getVideo(String videoId) throws IOException {
        // snippet : 제목, 설명, 썸네일 이미지 등
        // contentDetails: duration(ISO 8601 형식)
        YouTube.Videos.List data = youtube.videos().list(Collections.singletonList("snippet,contentDetails"));
        // videoId 지정
        data.setId(Collections.singletonList(videoId));
        // apikey 지정
        data.setKey(apiKey);
//        VideoListResponse result = data.execute();
//        List<Video> videoList = result.getItems();
//        Video video = videoList.get(0);
        Video video = data.execute().getItems().get(0);

        TrackDto trackDto = new TrackDto();
        // 제목, 가수 명, 길이, 이미지 url 가져오기
        trackDto.setTitle(video.getSnippet().getTitle());
        trackDto.setArtist(video.getSnippet().getChannelTitle());   // 일단 채널 명
        trackDto.setImageUrl(video.getSnippet().getThumbnails().getDefault().getUrl());
        trackDto.setDuration(Duration.parse(video.getContentDetails().getDuration()).toMillis());

        // 트랙 저장
        trackRepository.save(new Track(trackDto));

        // 비디오 리스트를 VideoDto로 변환
        return trackDto;
    }
}