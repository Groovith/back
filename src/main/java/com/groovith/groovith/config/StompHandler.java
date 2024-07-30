package com.groovith.groovith.config;

import com.groovith.groovith.exception.UnauthorizedException;
import com.groovith.groovith.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private static final String BEARER_PREFIX = "Bearer ";


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        log.info("accessor : {}" , accessor.getFirstNativeHeader("Authorization"));
        // 웹소켓 연결시 Stomp 메세지 헤더의 Authorization 에 담긴 jwtToken 검증
        if (StompCommand.CONNECT == accessor.getCommand()){
            //String token = accessor.getFirstNativeHeader("access");

            //access -> Authorization 변경시 추가 코드
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token == null) {
                throw new UnauthorizedException("Authorization 헤더가 없습니다.");
            } else if (token.isEmpty()) {
                throw new UnauthorizedException("Authorization 헤더에 값이 없습니다.");
            } else if (token.startsWith(BEARER_PREFIX)) {
                token = token.substring(BEARER_PREFIX.length());
            }

            // userId 를 토큰에서부터 받아오게끔 수정
            Long userId = jwtUtil.getUserId(token);
            log.info("userId:{}", userId);

            jwtUtil.validateToken(token, userId);
        }
        return message;
    }
}
