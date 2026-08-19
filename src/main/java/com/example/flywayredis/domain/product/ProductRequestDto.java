package com.example.flywayredis.domain.product;

public record ProductRequestDto(
        Long sellerId,
        String title,
        String content,
        Integer price,
        String status
){}
