//package com.groovith.groovith.controller;
//
//import com.groovith.groovith.domain.enums.StreamingType;
//import com.groovith.groovith.domain.User;
//import com.groovith.groovith.dto.ConnectSpotifyRequestDto;
//import com.groovith.groovith.dto.SpotifyTokenResponseDto;
//import com.groovith.groovith.dto.StreamingTypeResponseDto;
//import com.groovith.groovith.exception.UserNotFoundException;
//import com.groovith.groovith.security.CustomUserDetails;
//import com.groovith.groovith.service.SpotifyService;
//import com.groovith.groovith.service.UserService;
//import lombok.AllArgsConstructor;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
///**
// * 외부 스트리밍 서비스와 연결 설정 및 유저의 서비스 종류에 따라 필요 API 를 호출하고 결과를 반환해주는 컨트롤러.
// */
//@RestController
//@AllArgsConstructor
//@RequestMapping("/api/streaming")
//public class StreamingController {
//
//    private final UserService userService;
//    private final SpotifyService spotifyService;
//
//    /**
//     * 현재 유저가 연결하여 사용 중인 스트리밍 서비스 정보 반환.
//     *
//     * @param accessToken 서버 Access 토큰, 유저 정보 파악
//     * @return 성공 시 200, 스티리밍 서비스 정보(없는 경우 NONE)
//     */
//    @GetMapping()
//    ResponseEntity<StreamingTypeResponseDto> getStreaming(@AuthenticationPrincipal CustomUserDetails userDetails) {
//        User user = userDetails.getUser();
//        StreamingTypeResponseDto responseDto = new StreamingTypeResponseDto();
//        switch (user.getStreaming()) {
//            case NONE -> responseDto.setType(StreamingType.NONE);
//            case SPOTIFY -> responseDto.setType(StreamingType.SPOTIFY);
//        }
//
//        return new ResponseEntity<>(responseDto, HttpStatus.OK);
//    }
//
//    /**
//     * Spotify API 를 유저 계정에 연결.
//     * 기존에 연결된 다른 서비스가 있는 경우 연결 해제.
//     * 클라이언트에서 OAuth2.0 인증 후 Callback URL Search Parameter 로 받은 code 를 이용해서 Spotify 에 토큰 요청
//     * Spotify Refresh 토큰을 User 테이블에 저장하여 관리.
//     * User 의 Streaming 서비스를 SPOTIFY 로 설정.
//     *
//     * @param requestDto callback url parameter 에서 제공받는 값.
//     * @return 성공 시 200(Ok) | code 가 없거나 유효하지 않은 경우 400(Bad Request) | 토큰 요청 시도 중 오류 발생 시 500
//     */
//    @PostMapping("/spotify")
//    ResponseEntity<SpotifyTokenResponseDto> connectSpotify(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody ConnectSpotifyRequestDto requestDto) {
//        String code = requestDto.getCode();
//        SpotifyTokenResponseDto responseDto = new SpotifyTokenResponseDto();
//
//        // code 가 비어있는 경우
//        if (code.equals("")) {
//            responseDto.setMessage("code 가 없습니다.");
//            return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
//        }
//
//        try {
//            User user = userDetails.getUser();
//
//            // code 통해서 토큰 발행 요청
//            responseDto = spotifyService.requestSpotifyTokens(user, code);
//            return new ResponseEntity<>(responseDto, HttpStatus.OK);
//        } catch (UserNotFoundException e) {
//            // 유저 없음
//            responseDto.setMessage(e.getMessage());
//            return new ResponseEntity<>(responseDto, HttpStatus.BAD_REQUEST);
//        } catch (Exception e) {
//            // 서버 에러
//            responseDto.setMessage(e.getMessage());
//            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    /**
//     * 새롭게 발급 받은 Spotify 토큰 반환
//     *
//     * @param accessToken user 토큰
//     * @return SpotifyTokensResponseDto
//     */
//    @GetMapping("/spotify")
//    ResponseEntity<SpotifyTokenResponseDto> getTokens(@AuthenticationPrincipal CustomUserDetails userDetails) {
//        User user = userDetails.getUser();
//        return new ResponseEntity<>(spotifyService.refreshSpotifyTokens(user), HttpStatus.OK);
//    }
//
//    /**
//     * Spotify API 를 유저 계정과 연결 해제.
//     * User 의 Streaming 서비스를 NONE 으로 변경.
//     * User 테이블에서 Spotify 토큰 삭제.
//     *
//     * @param accessToken 서버 Access 토큰, 유저 정보 파악
//     * @return 성공 시 200(Ok) | 연결되어 있지 않은 경우 400(Bad Request) + message | 서버 오류 시 500(Internal Server Error) + message
//     */
//    @DeleteMapping("/spotify")
//    ResponseEntity<?> disconnectSpotify(@AuthenticationPrincipal CustomUserDetails userDetails) {
//        try {
//            User user = userDetails.getUser();
//            if (user.getStreaming() != StreamingType.SPOTIFY) {
//                return new ResponseEntity<>("Spotify에 연결되어 있지 않습니다.", HttpStatus.BAD_REQUEST);
//            }
//
//            userService.removeSpotifyToken(user.getId());
//
//            return new ResponseEntity<>(HttpStatus.OK);
//        } catch (UserNotFoundException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        } catch (Exception e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//}
