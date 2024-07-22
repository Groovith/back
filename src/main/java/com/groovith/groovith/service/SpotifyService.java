package com.groovith.groovith.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Spotify 스트리밍 서비스를 이용하는 경우 Spotify API 와 상호작용하는 서비스 클래스.
 */
@Slf4j
@Service
public class SpotifyService {

    @Value("${spotify.client-id}")
    private String clientId;
    @Value("${spotify.client-secret}")
    private String clientSecret;
    @Value("${spotify.redirect-uri}")
    private String redirectUri;

    public SpotifyService() {

    }

    /**
     * Spotify 토큰 요청
     * 참조: https://developer.spotify.com/documentation/web-api/tutorials/code-flow
     *
     * @param code 사용자로부터 전달받는 인증 코드. Spotify OAuth 인증 후 Callback url 에서 파라미터로 확인 가능.
     * @return Spotify access_token 과 refresh_token 반환
     */
    public Map<String, String> requestSpotifyTokens(String code) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://accounts.spotify.com/api/token";

        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedCredentials);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Spotify 토큰 요청 오류" + response.getBody());
        }

        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", (String) Objects.requireNonNull(response.getBody()).get("access_token"));
        tokens.put("refresh_token", (String) Objects.requireNonNull(response.getBody()).get("refresh_token"));

        return tokens;
    }

    public String searchTrack(String accessToken, String query) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.spotify.com/v1/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&type=track";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Spotify Search Track fetch 오류");
        }

        return response.getBody();
    }
}
