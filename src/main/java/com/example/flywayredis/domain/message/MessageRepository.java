package com.example.flywayredis.domain.message;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findAllByChatRoomIdOrderByCreatedAtAsc(Long chatRoomId);
}
