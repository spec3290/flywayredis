package com.example.flywayredis.service;

import com.example.flywayredis.dto.chat.ChatMessageRequest;
import com.example.flywayredis.dto.chat.ChatMessageResponse;
import com.example.flywayredis.dto.chat.ChatRoomCreateRequest;
import com.example.flywayredis.dto.chat.ChatRoomResponse;
import com.example.flywayredis.entity.ChatRoom;
import com.example.flywayredis.repository.ChatRoomRepository;

import com.example.flywayredis.entity.Message;
import com.example.flywayredis.repository.MessageRepository;
import com.example.flywayredis.entity.Product;
import com.example.flywayredis.repository.ProductRepository;
import com.example.flywayredis.entity.User;
import com.example.flywayredis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class ChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public ChatRoomResponse createOrGetRoom(Long loginUserId, ChatRoomCreateRequest request) {
        if (request.productId() == null) {
            throw new IllegalArgumentException("상품 ID는 필수입니다.");
        }

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + request.productId()));
        User buyer = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("구매자를 찾을 수 없습니다: " + loginUserId));

        if (Objects.equals(product.getSellerId(), buyer.getId())) {
            throw new IllegalArgumentException("판매자는 자신의 상품에 채팅방을 만들 수 없습니다.");
        }

        ChatRoom chatRoom = chatRoomRepository.findByProductIdAndBuyerId(product.getId(), buyer.getId())
                .orElseGet(() -> chatRoomRepository.save(ChatRoom.create(product, buyer)));
        return ChatRoomResponse.from(chatRoom);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getRooms(Long userId) {
        return chatRoomRepository.findAllByBuyerIdOrProductSellerIdOrderByCreatedAtDesc(userId, userId).stream()
                .map(ChatRoomResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long loginUserId, Long roomId) {
        requireParticipant(roomId, loginUserId);
        return messageRepository.findAllByChatRoomIdOrderByCreatedAtAsc(roomId).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long loginUserId, Long roomId, ChatMessageRequest request) {
        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("메시지 내용은 비어 있을 수 없습니다.");
        }

        ChatRoom chatRoom = requireRoom(roomId);
        User sender = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("보낸 사용자를 찾을 수 없습니다: " + loginUserId));

        if (!isParticipant(chatRoom, sender.getId())) {
            throw new IllegalArgumentException("채팅방 참여자만 메시지를 보낼 수 있습니다.");
        }

        Message message = messageRepository.save(Message.create(chatRoom, sender, request.content().trim()));
        return ChatMessageResponse.from(message);
    }

    private ChatRoom requireRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다: " + roomId));
    }

    @Transactional(readOnly = true)
    public void requireParticipant(Long roomId, Long userId) {
        ChatRoom chatRoom = requireRoom(roomId);
        if (!isParticipant(chatRoom, userId)) {
            throw new IllegalArgumentException("채팅방 참여자만 접근할 수 있습니다.");
        }
    }

    private boolean isParticipant(ChatRoom chatRoom, Long userId) {
        return Objects.equals(chatRoom.getBuyerId(), userId)
                || Objects.equals(chatRoom.getProduct().getSellerId(), userId);
    }

}
