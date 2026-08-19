package com.example.flywayredis.domain.chat;

public record ChatMessageRequest(Long senderId, String content) {
}
