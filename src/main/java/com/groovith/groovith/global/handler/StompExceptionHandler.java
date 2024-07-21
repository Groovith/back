package com.groovith.groovith.global.handler;

import com.groovith.groovith.global.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

// stomp에서 예외 발생시 처리
@Slf4j
@Component
public class StompExceptionHandler extends StompSubProtocolErrorHandler {
    public StompExceptionHandler(){
        super();
    }

    // 웹소켓에서 에러 발생시 호출
    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {

        log.info("clientMessage: "+clientMessage);
        log.info("Throwable: "+ex.getCause());
        // MessageDeliveryException 일 경우 UnauthorizedException
        if(ex instanceof MessageDeliveryException){
            ex = ex.getCause();
        }
        if(ex instanceof UnauthorizedException){    // stompHandler 에서 터트린 예외일경우
            // 메세지에 커스텀 헤더 추가 (http.status)
            StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
            stompHeaderAccessor.setMessage(ex.getMessage());
            stompHeaderAccessor.setHeader("HttpStatus", 401);
            byte[] errorPayload = ex.getMessage().getBytes();
            log.info("stompHeader: " + stompHeaderAccessor);
            return MessageBuilder.createMessage(errorPayload, stompHeaderAccessor.getMessageHeaders());
        }


        return super.handleClientMessageProcessingError(clientMessage, ex);
    }

    @Override
    protected Message<byte[]> handleInternal(StompHeaderAccessor errorHeaderAccessor,
                                             byte[] errorPayload,  Throwable cause,
                                             StompHeaderAccessor clientHeaderAccessor){
        return MessageBuilder.createMessage(errorPayload, errorHeaderAccessor.getMessageHeaders());
    }

}
