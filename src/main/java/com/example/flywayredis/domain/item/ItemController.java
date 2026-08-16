package com.example.flywayredis.domain.item;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ItemController {

    private final ItemService itemService;

    @Operation(summary = "검색 api", description = "검색 결과가 레디스 캐시에 저장되는 검색 api입니다.")
    @GetMapping("/{id}")
    public ItemResponseDto getItem(@PathVariable Long id){
        return itemService.getItem(id);
    }

}
