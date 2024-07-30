package com.groovith.groovith.config;

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



    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel){
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        // 웹소켓 연결시 Stomp 메세지 헤더의 Authorization 에 담긴 jwtToken 검증
        if (StompCommand.CONNECT == accessor.getCommand()){
            // userId 를 토큰에서부터 받아오게끔 수정
            Long userId = jwtUtil.getUserId(accessor.getFirstNativeHeader("access"));
            //String userId = accessor.getFirstNativeHeader("userId");

            log.info("userId:{}", userId);
            String token = accessor.getFirstNativeHeader("access");

            jwtUtil.validateToken(token, userId);
        }
        return message;
    }
}
