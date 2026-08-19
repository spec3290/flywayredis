package com.example.flywayredis.domain.chat;

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
    public ChatRoomResponse createOrGetRoom(@RequestBody ChatRoomCreateRequest request) {
        return chatService.createOrGetRoom(request);
    }

    @GetMapping
    public List<ChatRoomResponse> getRooms(@RequestParam Long userId) {
        return chatService.getRooms(userId);
    }

    @GetMapping("/{roomId}/messages")
    public List<ChatMessageResponse> getMessages(@PathVariable Long roomId) {
        return chatService.getMessages(roomId);
    }

    @MessageMapping("/chat-rooms/{roomId}/messages")
    public void sendMessage(
            @DestinationVariable Long roomId,
            ChatMessageRequest request
    ) {
        ChatMessageResponse message = chatService.sendMessage(roomId, request);
        messagingTemplate.convertAndSend("/sub/chat-rooms/" + roomId, message);
    }

}
