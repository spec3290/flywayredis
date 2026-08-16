package com.example.flywayredis.domain.item;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


class ItemServiceTest {

    @Test
    void 상품을_Id로_조회한다() {
        ItemRepository itemRepository = mock(ItemRepository.class);
        ItemService itemService = new ItemService(itemRepository);

        LocalDateTime createdAt = LocalDateTime.of(
                2026, 8, 16, 12, 0
        );

        Item item = mock(Item.class);

        when(item.getId()).thenReturn(1L);
        when(item.getName()).thenReturn("김지호");
        when(item.getPrice()).thenReturn(999);
        when(item.getCreatedAt()).thenReturn(createdAt);

        when(itemRepository.findById(1L))
                .thenReturn(Optional.of(item));

        ItemResponseDto result = itemService.getItem(1L);

        assertEquals(1L, result.id());
        assertEquals("김지호", result.name());
        assertEquals(999, result.price());
        assertEquals(createdAt, result.createdAt());

        verify(itemRepository).findById(1L);
    }

    @Test
    void 존재하지_않는_상품을_조회하면_예외가_발생한다() {
        // Given
        ItemRepository itemRepository = mock(ItemRepository.class);
        ItemService itemService = new ItemService(itemRepository);

        when(itemRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> itemService.getItem(999L)
        );
    }
}
