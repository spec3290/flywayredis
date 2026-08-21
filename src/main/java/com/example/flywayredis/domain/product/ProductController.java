package com.example.flywayredis.domain.product;

import com.example.flywayredis.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 조회 API", description = "상품 조회 결과가 Redis 캐시에 저장됩니다.")
    @GetMapping("/{id}")
    public ApiResponse<ProductResponseDto> getProduct(@PathVariable Long id) {
        return ApiResponse.success(productService.getProduct(id));
    }

    @Operation(summary = "상품 생성 API", description = "상품을 생성합니다.")
    @PostMapping
    public ApiResponse<ProductResponseDto> createProduct(@RequestBody ProductRequestDto request) {
        return ApiResponse.success(productService.createProduct(request));
    }

    @Operation(summary = "상품 수정 API", description = "판매자만 상품을 수정할 수 있습니다.")
    @PutMapping("/{id}")
    public ApiResponse<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequestDto request
    ) {
        return ApiResponse.success(productService.updateProduct(id, request));
    }

    @Operation(summary = "상품 삭제 API", description = "판매자만 상품을 삭제할 수 있습니다.")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(
            @PathVariable Long id,
            @RequestParam Long sellerId
    ) {
        productService.deleteProduct(id, sellerId);
        return ApiResponse.success(null);
    }
}
