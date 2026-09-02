package com.example.flywayredis.entity;

import com.example.flywayredis.dto.product.ProductRequestDto;

import com.example.flywayredis.entity.ChatRoom;
import com.example.flywayredis.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false, length = 50)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "product")
    private final List<ChatRoom> chatRooms = new ArrayList<>();

    public static Product create(User seller, ProductRequestDto request) {
        Product product = new Product();
        product.sellerId = seller.getId();
        product.title = request.title().trim();
        product.content = request.content();
        product.price = request.price();
        product.status = request.status().trim();
        return product;
    }

    public void update(ProductRequestDto request) {
        this.title = request.title().trim();
        this.content = request.content();
        this.price = request.price();
        this.status = request.status().trim();
    }
}
