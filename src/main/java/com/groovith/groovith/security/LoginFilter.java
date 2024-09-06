package com.groovith.groovith.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groovith.groovith.repository.RefreshRepository;
import com.groovith.groovith.domain.Refresh;
import com.groovith.groovith.dto.LoginDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthenticationService authenticationService;

    public LoginFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, AuthenticationService authenticationService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.authenticationService = authenticationService;
        // URL 설정
        this.setFilterProcessesUrl("/api/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        LoginDto loginDTO;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ServletInputStream inputStream = request.getInputStream();
            String messageBody = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            loginDTO = objectMapper.readValue(messageBody, LoginDto.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String email = loginDTO.getEmail();
        String password = loginDTO.getPassword();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);

        return authenticationManager.authenticate(authToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        //유저 정보
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        String username = authentication.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        // 기존 Refresh 토큰 삭제
        // 요청에서 refresh token 추출
        String oldRefresh = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    oldRefresh = cookie.getValue();
                }
            }
        }

        // 기존 Refresh 토큰이 있으면 삭제
        if (oldRefresh != null) {
            authenticationService.deleteRefresh(oldRefresh);
        }

        // 토큰 생성
        String access = jwtUtil.createJwt("access", userId, username, role, 86400000L);
        String refresh = jwtUtil.createJwt("refresh", userId, username, role, 604800000L);

        // Refresh 토큰 저장
        authenticationService.addRefresh(userId, refresh, 86400000L);

        // 응답 설정
        response.setHeader("Authorization", "Bearer " + access);
        response.addCookie(createCookie("refresh", refresh));
        response.setStatus(HttpStatus.OK.value());
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {

        response.setStatus(401);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        // https 통신시
        //cookie.setSecure(true);
        // 쿠키 범위 설정
        //cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}