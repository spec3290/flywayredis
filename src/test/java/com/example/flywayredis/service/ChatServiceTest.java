package com.example.flywayredis.service;

import com.example.flywayredis.dto.chat.ChatMessageRequest;
import com.example.flywayredis.dto.chat.ChatMessageResponse;
import com.example.flywayredis.entity.ChatRoom;
import com.example.flywayredis.repository.ChatRoomRepository;

import com.example.flywayredis.entity.Message;
import com.example.flywayredis.repository.MessageRepository;
import com.example.flywayredis.entity.Product;
import com.example.flywayredis.repository.ProductRepository;
import com.example.flywayredis.entity.User;
import com.example.flywayredis.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    @Test
    void 채팅방_참여자가_메시지를_전송하면_저장한다() {
        ChatRoomRepository chatRoomRepository = mock(ChatRoomRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        MessageRepository messageRepository = mock(MessageRepository.class);
        ChatService chatService = new ChatService(
                chatRoomRepository, productRepository, userRepository, messageRepository
        );

        ChatRoom chatRoom = mock(ChatRoom.class);
        Product product = mock(Product.class);
        User buyer = mock(User.class);
        User seller = mock(User.class);
        User sender = mock(User.class);
        Message savedMessage = mock(Message.class);
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 19, 12, 0);

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
        when(chatRoom.getId()).thenReturn(1L);
        when(chatRoom.getBuyer()).thenReturn(buyer);
        when(chatRoom.getProduct()).thenReturn(product);
        when(buyer.getId()).thenReturn(10L);
        when(product.getSeller()).thenReturn(seller);
        when(seller.getId()).thenReturn(20L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(sender));
        when(sender.getId()).thenReturn(10L);
        when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
        when(savedMessage.getId()).thenReturn(100L);
        when(savedMessage.getChatRoom()).thenReturn(chatRoom);
        when(savedMessage.getSender()).thenReturn(sender);
        when(savedMessage.getContent()).thenReturn("안녕하세요");
        when(savedMessage.getCreatedAt()).thenReturn(createdAt);

        ChatMessageResponse result = chatService.sendMessage(10L, 1L, new ChatMessageRequest(" 안녕하세요 "));

        assertEquals(100L, result.id());
        assertEquals(1L, result.roomId());
        assertEquals(10L, result.senderId());
        assertEquals("안녕하세요", result.content());
        assertEquals(createdAt, result.createdAt());
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void 채팅방_참여자가_아니면_메시지를_전송할_수_없다() {
        ChatRoomRepository chatRoomRepository = mock(ChatRoomRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        MessageRepository messageRepository = mock(MessageRepository.class);
        ChatService chatService = new ChatService(
                chatRoomRepository, productRepository, userRepository, messageRepository
        );

        ChatRoom chatRoom = mock(ChatRoom.class);
        Product product = mock(Product.class);
        User buyer = mock(User.class);
        User seller = mock(User.class);
        User sender = mock(User.class);

        when(chatRoomRepository.findById(1L)).thenReturn(Optional.of(chatRoom));
        when(chatRoom.getBuyer()).thenReturn(buyer);
        when(chatRoom.getProduct()).thenReturn(product);
        when(buyer.getId()).thenReturn(10L);
        when(product.getSeller()).thenReturn(seller);
        when(seller.getId()).thenReturn(20L);
        when(userRepository.findById(30L)).thenReturn(Optional.of(sender));
        when(sender.getId()).thenReturn(30L);

        assertThrows(
                IllegalArgumentException.class,
                () -> chatService.sendMessage(30L, 1L, new ChatMessageRequest("안녕하세요"))
        );
    }
}
