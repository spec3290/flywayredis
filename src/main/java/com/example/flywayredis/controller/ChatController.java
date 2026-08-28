package com.example.flywayredis.controller;

import com.example.flywayredis.dto.chat.ChatMessageRequest;
import com.example.flywayredis.dto.chat.ChatMessageResponse;
import com.example.flywayredis.dto.chat.ChatRoomCreateRequest;
import com.example.flywayredis.dto.chat.ChatRoomResponse;
import com.example.flywayredis.service.ChatService;

import com.example.flywayredis.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat-rooms")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ApiResponse<ChatRoomResponse> createOrGetRoom(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ChatRoomCreateRequest request
    ) {
        return ApiResponse.success(chatService.createOrGetRoom(userId(jwt), request));
    }

    @GetMapping
    public ApiResponse<List<ChatRoomResponse>> getRooms(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(chatService.getRooms(userId(jwt)));
    }

    @GetMapping("/{roomId}/messages")
    public ApiResponse<List<ChatMessageResponse>> getMessages(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long roomId
    ) {
        return ApiResponse.success(chatService.getMessages(userId(jwt), roomId));
    }

    @MessageMapping("/chat-rooms/{roomId}/messages")
    public void sendMessage(
            @DestinationVariable Long roomId,
            Authentication authentication,
            ChatMessageRequest request
    ) {
        JwtAuthenticationToken jwtAuthentication = (JwtAuthenticationToken) authentication;
        Long senderId = Long.valueOf(jwtAuthentication.getToken().getSubject());
        ChatMessageResponse message = chatService.sendMessage(senderId, roomId, request);
        messagingTemplate.convertAndSend(
                "/sub/chat-rooms/" + roomId,
                ApiResponse.success(message)
        );
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

}
