package com.example.flywayredis.dto.chat;

import com.example.flywayredis.entity.ChatRoom;

import java.time.LocalDateTime;

public record ChatRoomResponse(
        Long id,
        Long productId,
        Long buyerId,
        Long sellerId,
        LocalDateTime createdAt
) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getProduct().getId(),
                chatRoom.getBuyer().getId(),
                chatRoom.getProduct().getSeller().getId(),
                chatRoom.getCreatedAt()
        );
    }
}
