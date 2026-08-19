package com.example.flywayredis.domain.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByProductIdAndBuyerId(Long productId, Long buyerId);

    List<ChatRoom> findAllByBuyerIdOrProductSellerIdOrderByCreatedAtDesc(Long buyerId, Long sellerId);
}
