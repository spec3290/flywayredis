package com.example.flywayredis.domain.product;

public record ProductRequestDto(
        String title,
        String content,
        Integer price,
        String status
){}
