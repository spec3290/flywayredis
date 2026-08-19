CREATE TABLE IF NOT EXISTS chat_rooms (
    room_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_chat_rooms_product_buyer UNIQUE (product_id, buyer_id),
    CONSTRAINT fk_chat_rooms_product FOREIGN KEY (product_id) REFERENCES products (product_id),
    CONSTRAINT fk_chat_rooms_buyer FOREIGN KEY (buyer_id) REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS messages (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_messages_room FOREIGN KEY (room_id) REFERENCES chat_rooms (room_id),
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users (user_id)
);

DELIMITER $$

CREATE PROCEDURE add_chat_indexes_if_missing()
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'chat_rooms'
          AND index_name = 'uq_chat_rooms_product_buyer'
    ) THEN
        ALTER TABLE chat_rooms
            ADD CONSTRAINT uq_chat_rooms_product_buyer UNIQUE (product_id, buyer_id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
          AND table_name = 'messages'
          AND index_name = 'idx_messages_room_created_at'
    ) THEN
        CREATE INDEX idx_messages_room_created_at ON messages (room_id, created_at);
    END IF;
END$$

CALL add_chat_indexes_if_missing()$$

DROP PROCEDURE add_chat_indexes_if_missing$$

DELIMITER ;
