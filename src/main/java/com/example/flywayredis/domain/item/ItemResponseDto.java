package com.example.flywayredis.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ItemResponseDto(Long id, String name, int price, LocalDateTime createdAt) {
    public static ItemResponseDto from(Item item){
        return new ItemResponseDto(
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getCreatedAt()
        );
    }
}
