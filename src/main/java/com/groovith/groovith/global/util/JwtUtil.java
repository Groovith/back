package com.groovith.groovith.global.util;

import com.groovith.groovith.global.exception.UnauthorizedException;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private SecretKey secretKey;

    public JwtUtil(@Value("${spring.jwt.secret}")String secret) {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public  Long getUserId(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userId",Long.class);
    }

    public String getUsername(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public String getCategory(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public void validateToken(String token, String userId){
        if (token == null){
            throw new UnauthorizedException("헤더에 Authorization가 존재하지않습니다.");
        } else if ( token.isEmpty()) {
            throw new UnauthorizedException("헤더에 Authorization에 값이 존재하지않습니다.");
        }
        // 토큰 유효성 검증
        try{
            Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e){

            throw new UnauthorizedException("JWT 토큰값이 잘못되었습니다.", e);
        } catch (ExpiredJwtException e){

            throw new UnauthorizedException("만료된 JWT 토큰입니다", e);
        } catch (UnsupportedJwtException e) {

            throw new UnauthorizedException("지원되지 않는 JWT 토큰입니다", e);
        } catch (IllegalStateException e){

            throw new UnauthorizedException("JWT 토큰이 존재하지 않습니다", e);
        }
        // 웹소켓에 연결 시도한 유저와 토큰에서의 userId가 다를 경우
        if (getUserId(token)!=(Long.parseLong(userId))){

            throw new UnauthorizedException("잘못된 JWT 토큰입니다.");
        }
    }

    public String createJwt(String category, Long userId, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("category", category)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }
}
