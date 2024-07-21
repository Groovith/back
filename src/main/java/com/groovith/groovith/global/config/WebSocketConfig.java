package com.groovith.groovith.global.config;

import com.groovith.groovith.global.handler.StompExceptionHandler;
import com.groovith.groovith.global.handler.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;
    private final StompExceptionHandler stompExceptionHandler;

    // STOMP 엔드포인트 설정: 유저가 웹소켓과 연결할 url 설정
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        // 실제 구현에서는 추가하기
        // .withSockJS()
        registry.setErrorHandler(stompExceptionHandler)
                .addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:3000")
                .setAllowedOriginPatterns("*");
                //.withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config){

        // 메세지 보낼때 // 메세지 pub 요청 url : "/app" 접두사가 붙은 메시지 라우팅
        config.setApplicationDestinationPrefixes("/pub");
        // 메세지 받을때 // 메세지 sub 요청 url : 유저에게 메시지 전달할 때 사용할 목적지 접두사 설정, topic - 1대다, queue - 1대1
        config.enableSimpleBroker("/sub");
    }


    // 메세지 헤더에서 
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration){
        registration.interceptors(stompHandler);
    }
}
