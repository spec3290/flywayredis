package com.example.flywayredis.service;

import com.example.flywayredis.dto.product.ProductRequestDto;
import com.example.flywayredis.dto.product.ProductResponseDto;
import com.example.flywayredis.entity.Product;
import com.example.flywayredis.repository.ProductRepository;

import com.example.flywayredis.entity.User;
import com.example.flywayredis.repository.UserRepository;
import com.example.flywayredis.common.exception.BusinessException;
import com.example.flywayredis.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
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
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductResponseDto.from(product);
    }

    @Transactional
    public ProductResponseDto createProduct(Long loginUserId, ProductRequestDto request) {
        validateRequest(request);
        User seller = userRepository.findById(loginUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.save(Product.create(seller, request));
        return ProductResponseDto.from(product);
    }

    @Transactional
    @CachePut(cacheNames = "product", key = "#id")
    public ProductResponseDto updateProduct(Long loginUserId, Long id, ProductRequestDto request) {
        validateRequest(request);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        validateSeller(product, loginUserId);
        product.update(request);
        return ProductResponseDto.from(product);
    }

    @Transactional
    @CacheEvict(cacheNames = "product", key = "#id")
    public void deleteProduct(Long loginUserId, Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        validateSeller(product, loginUserId);
        productRepository.delete(product);
    }

    private void validateSeller(Product product, Long sellerId) {
        if (sellerId == null || !product.getSeller().getId().equals(sellerId)) {
            throw new BusinessException(ErrorCode.NOT_SELLER_OF_PRODUCT);
        }
    }

    private void validateRequest(ProductRequestDto request) {
        if (request == null || request.title() == null || request.title().isBlank()) {
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
