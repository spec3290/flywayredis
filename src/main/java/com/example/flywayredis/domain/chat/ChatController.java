package com.example.flywayredis.domain.chat;

import com.example.flywayredis.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat-rooms")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    public ApiResponse<ChatRoomResponse> createOrGetRoom(@RequestBody ChatRoomCreateRequest request) {
        return ApiResponse.success(chatService.createOrGetRoom(request));
    }

    @GetMapping
    public ApiResponse<List<ChatRoomResponse>> getRooms(@RequestParam Long userId) {
        return ApiResponse.success(chatService.getRooms(userId));
    }

    @GetMapping("/{roomId}/messages")
    public ApiResponse<List<ChatMessageResponse>> getMessages(@PathVariable Long roomId) {
        return ApiResponse.success(chatService.getMessages(roomId));
    }

    @MessageMapping("/chat-rooms/{roomId}/messages")
    public void sendMessage(
            @DestinationVariable Long roomId,
            ChatMessageRequest request
    ) {
        ChatMessageResponse message = chatService.sendMessage(roomId, request);
        messagingTemplate.convertAndSend(
                "/sub/chat-rooms/" + roomId,
                ApiResponse.success(message)
        );
    }

}
