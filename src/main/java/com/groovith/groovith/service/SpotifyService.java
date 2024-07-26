package com.groovith.groovith.service;

import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.SpotifyTokensResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
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
    private final UserService userService;

    public SpotifyService(UserService userService) {
        this.userService = userService;
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

    /**
     * Spotify 토큰 갱신 요청.
     * 성공 시 User 에 토큰 업데이트. 새 Access 토큰 반환.
     *
     * @param user 토큰 갱신이 필요한 User Entity
     * @return 새로 발급된 Access 토큰
     */
    public SpotifyTokensResponseDto refreshSpotifyTokens(User user) throws HttpClientErrorException {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://accounts.spotify.com/api/token";

        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedCredentials);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", user.getSpotifyRefreshToken());
        //body.add("client_id", clientId);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        String accessToken = (String) Objects.requireNonNull(response.getBody()).get("access_token");
        //String refreshToken = (String) Objects.requireNonNull(response.getBody()).get("refresh_token");

        userService.saveSpotifyTokens(user.getId(), accessToken, "");

        SpotifyTokensResponseDto responseDto = new SpotifyTokensResponseDto();
        responseDto.setSpotifyAccessToken(accessToken);
        //responseDto.setSpotifyRefreshToken(refreshToken);

        return responseDto;
    }

    /**
     * Spotify 공통 요청 처리 메서드.
     * Access 토큰 만료 시 자동으로 토큰 갱신 요청 후, 기존 요청 다시 시도.
     *
     * @param url          요청할 Spotify API URL
     * @param method       HTTP 메서드 (GET, POST 등)
     * @param request      요청 HttpEntity (헤더와 바디 포함)
     * @param responseType 응답 타입
     * @param user         요청을 보낼 사용자
     * @param <T>          반환 타입 (String, Map 등)
     * @return 응답 엔티티
     */
    private <T> ResponseEntity<T> executeSpotifyRequest(String url, HttpMethod method, HttpEntity<?> request, Class<T> responseType, User user) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<T> response;

        try {
            response = restTemplate.exchange(url, method, request, responseType);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                // 401 Unauthorized (Access Token 만료) 메시지 반환 시 토큰 갱신 시도
                String newAccessToken = refreshSpotifyTokens(user).getSpotifyAccessToken();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(newAccessToken);
                HttpEntity<?> newRequest = new HttpEntity<>(request.getBody(), headers);
                response = restTemplate.exchange(url, method, newRequest, responseType);
            } else {
                throw e;
            }
        }

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Spotify API 요청 오류: " + response.getStatusCode());
        }

        return response;
    }

    /**
     * 트랙 검색
     *
     * @param user  요청한 User Entity
     * @param query 검색 쿼리
     * @return 검색 결과
     */
    public String searchTrack(User user, String query) {
        String url = "https://api.spotify.com/v1/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&type=track";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(user.getSpotifyAccessToken());

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<String> response = executeSpotifyRequest(url, HttpMethod.GET, request, String.class, user);

        return response.getBody();
    }
}
