package com.example.flywayredis.global;

import com.example.flywayredis.domain.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Component
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    private static final Pattern SEND_DESTINATION =
            Pattern.compile("^/pub/chat-rooms/(\\d+)/messages$");
    private static final Pattern SUBSCRIBE_DESTINATION =
            Pattern.compile("^/sub/chat-rooms/(\\d+)$");

    private final ChatService chatService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (accessor.getCommand() == StompCommand.CONNECT) {
            authenticate(accessor);
        } else if (accessor.getCommand() == StompCommand.SEND) {
            authorizeDestination(accessor, SEND_DESTINATION);
        } else if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            authorizeDestination(accessor, SUBSCRIBE_DESTINATION);
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        if (!(accessor.getUser() instanceof JwtAuthenticationToken)) {
            throw new AccessDeniedException("WebSocket 쿠키 인증이 필요합니다.");
        }
    }

    private void authorizeDestination(
            StompHeaderAccessor accessor,
            Pattern allowedDestination
    ) {
        if (!(accessor.getUser() instanceof JwtAuthenticationToken authentication)) {
            throw new AccessDeniedException("WebSocket 인증이 필요합니다.");
        }

        String destination = accessor.getDestination();
        Matcher matcher = destination == null ? null : allowedDestination.matcher(destination);
        if (matcher == null || !matcher.matches()) {
            throw new AccessDeniedException("허용되지 않은 WebSocket 경로입니다.");
        }

        Long roomId = Long.valueOf(matcher.group(1));
        Long userId = Long.valueOf(authentication.getToken().getSubject());
        chatService.requireParticipant(roomId, userId);
    }
}
