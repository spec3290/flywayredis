CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nickname VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (user_id, nickname, email, password, created_at)
SELECT 1, 'legacy-seller', 'legacy-seller@local.invalid', 'MIGRATED_LEGACY_USER', CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE user_id = 1
);

DELIMITER $$

CREATE PROCEDURE migrate_item_to_product()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND table_name = 'item'
    ) THEN
        IF EXISTS (
            SELECT 1
            FROM information_schema.tables
            WHERE table_schema = DATABASE()
              AND table_name = 'products'
        ) THEN
            INSERT INTO products (seller_id, title, content, price, status, created_at)
            SELECT 1, name, NULL, price, 'AVAILABLE', created_at
            FROM item;

            DROP TABLE item;
        ELSE
            RENAME TABLE item TO products;

            ALTER TABLE products
                CHANGE COLUMN id product_id BIGINT NOT NULL AUTO_INCREMENT,
                CHANGE COLUMN name title VARCHAR(255) NOT NULL,
                DROP COLUMN quantity,
                ADD COLUMN seller_id BIGINT NOT NULL DEFAULT 1 AFTER product_id,
                ADD COLUMN content TEXT NULL AFTER title,
                ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE' AFTER price,
                ADD CONSTRAINT fk_products_seller FOREIGN KEY (seller_id) REFERENCES users (user_id);
        END IF;
    END IF;
END$$

CALL migrate_item_to_product()$$

DROP PROCEDURE migrate_item_to_product$$

DELIMITER ;
