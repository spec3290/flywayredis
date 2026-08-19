package com.example.flywayredis.domain.product;

import java.time.LocalDateTime;

public record ProductResponseDto(
        Long id,
        Long sellerId,
        String title,
        String content,
        Integer price,
        String status,
        LocalDateTime createdAt
) {
    public static ProductResponseDto from(Product product) {
        return new ProductResponseDto(
                product.getId(),
                product.getSeller().getId(),
                product.getTitle(),
                product.getContent(),
                product.getPrice(),
                product.getStatus(),
                product.getCreatedAt()
        );
    }
}
