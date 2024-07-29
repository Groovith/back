package com.groovith.groovith.dto;

import lombok.Data;

@Data
public class SpotifyTokenResponseDto {
    private String message;
    private String spotifyAccessToken;
}
