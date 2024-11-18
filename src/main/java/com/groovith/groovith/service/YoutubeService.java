package com.groovith.groovith.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.groovith.groovith.domain.Track;
import com.groovith.groovith.dto.TrackDto;
import com.groovith.groovith.exception.InvalidThumbnailUrlException;
import com.groovith.groovith.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

@Transactional
@RequiredArgsConstructor
@Service
public class YoutubeService {

    private static final String TOPIC = " - Topic";
    private static final String INVALID_THUMBNAIL_URL = "유효한 썸네일 해상도가 없습니다.";

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
        trackDto.setArtist(parsingArtist(video.getSnippet().getChannelTitle()));
        trackDto.setImageUrl(getBestThumbnailUrl(video.getSnippet().getThumbnails()));
        trackDto.setDuration(Duration.parse(video.getContentDetails().getDuration()).toSeconds());
        return trackDto;
    }


    private String parsingArtist(String title){
        if (title.endsWith(TOPIC)) {
            return title.substring(0, title.length() - TOPIC.length());
        }
        return title;
    }

    /**
     * 썸네일 해상도 - 해당 영상이 지원하는 최대 해상도 선택
     *  1. maxres   : 1280x720(최대)
     *  2. standard : 640x480
     *  3. high     : 480x360
     *  4. medium   : 320x180
     *  5. default  : 120x90
     * */
    private String getBestThumbnailUrl(ThumbnailDetails thumbnails){
        return Stream.of(
                        thumbnails.getMaxres(),
                        thumbnails.getStandard(),
                        thumbnails.getHigh(),
                        thumbnails.getMedium(),
                        thumbnails.getDefault()
                )
                .filter(Objects::nonNull)
                .map(Thumbnail::getUrl)
                .findFirst()
                .orElseThrow(()-> new InvalidThumbnailUrlException(INVALID_THUMBNAIL_URL));
    }
}