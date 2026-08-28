package com.example.flywayredis.repository;

import com.example.flywayredis.entity.ChatRoom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByProductIdAndBuyerId(Long productId, Long buyerId);

    List<ChatRoom> findAllByBuyerIdOrProductSellerIdOrderByCreatedAtDesc(Long buyerId, Long sellerId);
}
