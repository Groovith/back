package com.groovith.groovith.dto;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.List;
@Embeddable
@Data
public class SpotifyTrackDto {
    private String album;
    private List<String> artists;
    private int disc_number;
    private Long duration_ms;
    private boolean explicit;
    private String external_ids;
    private String external_urls;
    private String href;
    private String id;
    private boolean is_playable;
    private String linked_from;
    private String restrictions;
    private String name;
    private int popularity;
    private String preview_url;
    private int track_number;
    private String type;
    private String uri;
    private boolean is_local;
}