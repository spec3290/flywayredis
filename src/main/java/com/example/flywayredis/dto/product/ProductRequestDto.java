package com.example.flywayredis.dto.product;

public record ProductRequestDto(
        String title,
        String content,
        Integer price,
        String status
){}
