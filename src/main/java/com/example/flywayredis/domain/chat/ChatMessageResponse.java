package com.example.flywayredis.domain.chat;

import com.example.flywayredis.domain.message.Message;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long roomId,
        Long senderId,
        String content,
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(Message message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getChatRoom().getId(),
                message.getSender().getId(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
