package com.groovith.groovith.dto;

import lombok.Data;

@Data
public class SpotifyTokensResponseDto {
    private String message;
    private String spotifyAccessToken;
    private String spotifyRefreshToken;
}
