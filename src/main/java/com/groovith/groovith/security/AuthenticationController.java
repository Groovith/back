package com.groovith.groovith.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        try {
            authenticationService.handleReissue(request, response);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Access Token 유효성 검사
     * 검사는 필터 단에서 이루어지며 단순 호출용 엔드포인트
     *
     * @return 유효한 토큰 -> 200 | 유효하지 않은 토큰 -> 401
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validate() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
