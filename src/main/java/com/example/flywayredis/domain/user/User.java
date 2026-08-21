package com.example.flywayredis.domain.user;

import com.example.flywayredis.domain.chat.ChatRoom;
import com.example.flywayredis.domain.message.Message;
import com.example.flywayredis.domain.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "seller")
    private final List<Product> sellingProducts = new ArrayList<>();

    @OneToMany(mappedBy = "buyer")
    private final List<ChatRoom> chatRooms = new ArrayList<>();

    @OneToMany(mappedBy = "sender")
    private final List<Message> sentMessages = new ArrayList<>();

    public static User create(String nickname, String email, String encodedPassword) {
        User user = new User();
        user.nickname = nickname;
        user.email = email;
        user.password = encodedPassword;
        return user;
    }
}
