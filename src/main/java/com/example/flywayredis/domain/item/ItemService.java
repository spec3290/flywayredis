package com.example.flywayredis.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ItemService {

    private final ItemRepository itemRepository;

    @Cacheable(cacheNames = "item", key = "#id")
    public ItemResponseDto getItem(Long id){
        Item item = itemRepository.findById(id)
                .orElseThrow(()->new IllegalArgumentException(
                        "상품을 찾을 수 없습니다" + id
                ));
        return ItemResponseDto.from(item);
    }
}
