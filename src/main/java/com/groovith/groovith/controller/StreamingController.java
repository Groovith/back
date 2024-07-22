package com.groovith.groovith.controller;

import com.groovith.groovith.domain.StreamingType;
import com.groovith.groovith.domain.User;
import com.groovith.groovith.dto.ConnectSpotifyRequestDto;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.service.SpotifyService;
import com.groovith.groovith.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 외부 스트리밍 서비스와 연결 설정 및 유저의 서비스 종류에 따라 필요 API 를 호출하고 결과를 반환해주는 컨트롤러.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/streaming")
public class StreamingController {

    private final UserService userService;
    private final SpotifyService spotifyService;

    /**
     * 현재 유저가 연결하여 사용 중인 스트리밍 서비스 정보 반환.
     *
     * @param accessToken 서버 Access 토큰, 유저 정보 파악
     * @return 성공 시 200, 스티리밍 서비스 정보(없는 경우 NONE)
     */
    @GetMapping()
    ResponseEntity<?> getStreaming(@RequestHeader("access") String accessToken) {
        User user = userService.getUser(accessToken);
        return new ResponseEntity<>(user.getStreaming().toString(), HttpStatus.OK);
    }

    /**
     * Spotify API 를 유저 계정에 연결.
     * 기존에 연결된 다른 서비스가 있는 경우 연결 해제.
     * 클라이언트에서 OAuth2.0 인증 후 Callback URL Search Parameter 로 받은 code 를 이용해서 Spotify 에 토큰 요청
     * Spotify Access Token 과 Refresh 토큰을 User 테이블에 저장하여 관리.
     * User 의 Streaming 서비스를 SPOTIFY 로 설정.
     *
     * @param code callback url parameter 에서 제공받는 값.
     * @return 성공 시 200(Ok) | code 가 없거나 유효하지 않은 경우 400(Bad Request) | 토큰 요청 시도 중 오류 발생 시 500
     */
    @PostMapping("/spotify")
    ResponseEntity<?> connectSpotify(@RequestHeader("access") String accessToken, @RequestBody ConnectSpotifyRequestDto requestDto) {
        String code = requestDto.getCode();

        // code 가 비어있는 경우
        if (code.equals("")) {
            return new ResponseEntity<>("code 가 없습니다.", HttpStatus.BAD_REQUEST);
        }

        try {
            User user = userService.getUser(accessToken);

            // 이미 스포티파이에 연결되어 있는 경우 에러 반환 -> 재연결 필요한 경우엔?
            if (user.getStreaming() == StreamingType.SPOTIFY) {
                return new ResponseEntity<>("이미 Spotify에 연결되어 있습니다.", HttpStatus.BAD_REQUEST);
            }

            // code 통해서 토큰 발행 요청
            Map<String, String> tokens = spotifyService.requestSpotifyTokens(code);
            userService.saveSpotifyTokens(user.getId(), tokens.get("access_token"), tokens.get("refresh_token"));

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (UserNotFoundException e) {
            // 유저 없음
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // 서버 에러
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Spotify API 를 유저 계정과 연결 해제.
     * User 의 Streaming 서비스를 NONE 으로 변경.
     * User 테이블에서 Spotify 토큰 삭제.
     *
     * @param accessToken 서버 Access 토큰, 유저 정보 파악
     * @return 성공 시 200(Ok) | 연결되어 있지 않은 경우 400(Bad Request) + message | 서버 오류 시 500(Internal Server Error) + message
     */
    @DeleteMapping("/spotify")
    ResponseEntity<?> disconnectSpotify(@RequestHeader("access") String accessToken) {
        try {
            User user = userService.getUser(accessToken);
            if (user.getStreaming() != StreamingType.SPOTIFY) {
                return new ResponseEntity<>("Spotify에 연결되어 있지 않습니다.", HttpStatus.BAD_REQUEST);
            }

            userService.removeSpotifyTokens(user.getId());

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 검색 API -> 현재 트랙만 반환
     * Access Token 을 이용해 연결된 스트리밍 서비스를 판별.
     * 해당하는 서비스를 이용해 검색 요청 후 결과 반환.
     *
     * @param accessToken 서버 Access 토큰, 유저 정보 파악
     * @param query       검색 쿼리
     * @return 성공 시 200(Ok) + 검색 결과 | 연결된 서비스가 없을 시 400(Bad Request) + message | 서버 오류 시 500(Internal Server Error) + message | query를 작성하지 않아 주소가 다른 경우 403
     */
    @GetMapping("/search")
    ResponseEntity<?> search(@RequestHeader("access") String accessToken, @RequestParam(value = "query") String query) {
        // 검색 쿼리가 빈 경우 -> 빈 결과 반환
        if (query.trim().equals("")) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        try {
            User user = userService.getUser(accessToken);
            switch (user.getStreaming()) {
                case NONE -> { // 연결된 서비스가 없는 경우
                    return new ResponseEntity<>("연결된 서비스가 없습니다.", HttpStatus.BAD_REQUEST);
                }
                case SPOTIFY -> {
                    String response = spotifyService.searchTrack(user.getSpotifyAccessToken(), query);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }
            }
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(query, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
