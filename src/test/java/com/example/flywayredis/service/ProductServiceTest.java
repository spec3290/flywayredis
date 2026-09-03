package com.example.flywayredis.service;

import com.example.flywayredis.dto.product.ProductRequestDto;
import com.example.flywayredis.dto.product.ProductResponseDto;
import com.example.flywayredis.entity.Product;
import com.example.flywayredis.repository.ProductRepository;

import com.example.flywayredis.entity.User;
import com.example.flywayredis.repository.UserRepository;
import com.example.flywayredis.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceTest {

    @Test
    void 상품_목록을_최신순으로_조회한다() {
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ProductService productService = new ProductService(productRepository, userRepository);
        Product product = mock(Product.class);
        User seller = mock(User.class);

        when(product.getId()).thenReturn(1L);
        when(product.getSellerId()).thenReturn(10L);
        when(seller.getId()).thenReturn(10L);
        when(product.getTitle()).thenReturn("키보드");
        when(product.getPrice()).thenReturn(24_000);
        when(product.getStatus()).thenReturn("AVAILABLE");
        when(productRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(product));

        List<ProductResponseDto> result = productService.getProducts();

        assertEquals(1, result.size());
        assertEquals("키보드", result.getFirst().title());
        verify(productRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void 상품_생성자는_요청값이_아니라_로그인_사용자를_사용한다() {
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ProductService productService = new ProductService(productRepository, userRepository);
        User loginUser = mock(User.class);

        when(loginUser.getId()).thenReturn(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(loginUser));
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponseDto result = productService.createProduct(
                10L,
                new ProductRequestDto("키보드", "기계식", 24_000, "AVAILABLE")
        );

        assertEquals(10L, result.sellerId());
        verify(userRepository).findById(10L);
    }

    @Test
    void 상품을_Id로_조회한다() {
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ProductService productService = new ProductService(productRepository, userRepository);
        Product product = mock(Product.class);
        User seller = mock(User.class);
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 16, 12, 0);

        when(product.getId()).thenReturn(1L);
        when(product.getSellerId()).thenReturn(1L);
        when(seller.getId()).thenReturn(10L);
        when(product.getTitle()).thenReturn("키보드");
        when(product.getContent()).thenReturn("기계식 키보드입니다.");
        when(product.getPrice()).thenReturn(24_000);
        when(product.getStatus()).thenReturn("AVAILABLE");
        when(product.getCreatedAt()).thenReturn(createdAt);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponseDto result = productService.getProduct(1L);

        assertEquals(1L, result.id());
        assertEquals(10L, result.sellerId());
        assertEquals("키보드", result.title());
        assertEquals(24_000, result.price());
        assertEquals(createdAt, result.createdAt());
        verify(productRepository).findById(1L);
    }

    @Test
    void 존재하지_않는_상품을_조회하면_예외가_발생한다() {
        ProductRepository productRepository = mock(ProductRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ProductService productService = new ProductService(productRepository, userRepository);

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> productService.getProduct(999L));
    }
}
