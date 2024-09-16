package com.groovith.groovith.security;

import com.groovith.groovith.domain.User;
import com.groovith.groovith.exception.UserNotFoundException;
import com.groovith.groovith.repository.RefreshRepository;
import com.groovith.groovith.domain.Refresh;
import com.groovith.groovith.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final UserRepository userRepository;

    @Transactional
    public void handleReissue(HttpServletRequest request, HttpServletResponse response) {
        //get refresh token
        String refresh = getRefreshToken(request);

        if (refresh == null) {
            throw new IllegalArgumentException("refresh token null");
        }

        //expired check
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("refresh token expired");
        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            throw new IllegalArgumentException("category is not refresh token");
        }

        //DB에 저장되어 있는지 확인
        Boolean isExist = refreshRepository.existsById(refresh);
        if (!isExist) {
            throw new IllegalArgumentException("refresh Token doesn't exist");
        }

        Long userId = jwtUtil.getUserId(refresh);
        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        //make new JWT
        String newAccess = jwtUtil.createJwt("access", userId, username, role, 86400000L);        // 24시간 유효
        String newRefresh = jwtUtil.createJwt("refresh", userId, username, role, 604800000L);   // 7일 유효

        //Refresh 토큰 저장 DB에 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
        refreshRepository.deleteById(refresh);
        addRefresh(userId, newRefresh);

        //response
        response.setHeader("Authorization", "Bearer " + newAccess);
        response.addCookie(createCookie("refresh", newRefresh));
    }

    public String getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public void addRefresh(Long userId, String refresh) {
        // User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        // Date date = new Date(System.currentTimeMillis() + expiredMs);

        Refresh newRefresh = new Refresh(refresh, userId);
//        newRefresh.setUserId(user.getId());
//        newRefresh.setRefresh(refresh);
//        newRefresh.setExpiration(date.toString());
        refreshRepository.save(newRefresh);
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        //cookie.setSecure(true);
        //cookie.setPath("/");
        cookie.setHttpOnly(true);
        return cookie;
    }

    /**
     * Refresh 토큰 삭제
     *
     * @param refresh String refresh 토큰
     */
    @Transactional
    public void deleteRefresh(String refresh) {
        refreshRepository.deleteById(refresh);
    }

    /**
     * Refresh 토큰 유효성 검사 -> RefreshRepository 에 있는 경우 true
     *
     * @param refresh String Refresh token
     * @return true | false
     */
    @Transactional(readOnly = true)
    public boolean isRefreshTokenValid(String refresh) {
        return refreshRepository.existsById(refresh);
    }
}
