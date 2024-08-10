package com.groovith.groovith.dto;

import lombok.Data;

import java.util.List;

@Data
public class SpotifyTrackDto {
    private Object album;
    private List<Object> artists;
    private int disc_number;
    private Long duration_ms;
    private boolean explicit;
    private Object external_ids;
    private Object external_urls;
    private String href;
    private String id;
    private boolean is_playable;
    private Object linked_from;
    private Object restrictions;
    private String name;
    private int popularity;
    private String preview_url;
    private int track_number;
    private String type;
    private String uri;
    private boolean is_local;
}