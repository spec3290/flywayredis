package com.example.flywayredis.domain.product;

import com.example.flywayredis.domain.user.User;
import com.example.flywayredis.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Cacheable(cacheNames = "product", key = "#id")
    public ProductResponseDto getProduct(Long id) {
        return ProductResponseDto.from(requireProduct(id));
    }

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto request) {
        validateRequest(request);
        User seller = userRepository.findById(request.sellerId())
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다: " + request.sellerId()));

        Product product = productRepository.save(Product.create(seller, request));
        return ProductResponseDto.from(product);
    }

    @Transactional
    @CacheEvict(cacheNames = "product", key = "#id")
    public ProductResponseDto updateProduct(Long id, ProductRequestDto request) {
        validateRequest(request);
        Product product = requireProduct(id);
        validateSeller(product, request.sellerId());
        product.update(request);
        return ProductResponseDto.from(product);
    }

    @Transactional
    @CacheEvict(cacheNames = "product", key = "#id")
    public void deleteProduct(Long id, Long sellerId) {
        Product product = requireProduct(id);
        validateSeller(product, sellerId);
        productRepository.delete(product);
    }

    private Product requireProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));
    }

    private void validateSeller(Product product, Long sellerId) {
        if (sellerId == null || !product.getSeller().getId().equals(sellerId)) {
            throw new IllegalArgumentException("상품 판매자만 변경하거나 삭제할 수 있습니다.");
        }
    }

    private void validateRequest(ProductRequestDto request) {
        if (request == null || request.sellerId() == null) {
            throw new IllegalArgumentException("판매자 ID는 필수입니다.");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("상품 제목은 필수입니다.");
        }
        if (request.price() == null || request.price() < 0) {
            throw new IllegalArgumentException("상품 가격은 0 이상이어야 합니다.");
        }
        if (request.status() == null || request.status().isBlank()) {
            throw new IllegalArgumentException("상품 상태는 필수입니다.");
        }
    }
}
