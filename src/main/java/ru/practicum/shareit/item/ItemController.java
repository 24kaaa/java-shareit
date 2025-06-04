package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto create(
            @Valid @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") int ownerId) {
        Item item = ItemMapper.toEntity(itemDto);
        Item createdItem = itemService.create(item, ownerId);
        return ItemMapper.toDto(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(
            @PathVariable int itemId,
            @Valid @RequestBody ItemUpdateDto updateDto,
            @RequestHeader("X-Sharer-User-Id") int ownerId) {

        Item updateData = ItemMapper.toEntity(updateDto);
        Item updatedItem = itemService.update(itemId, updateData, ownerId);
        return ItemMapper.toDto(updatedItem);
    }

    @GetMapping("/{itemId}")
    public ItemDto getById(@PathVariable int itemId) {
        return ItemMapper.toDto(itemService.getById(itemId));
    }

    @GetMapping
    public List<ItemDto> getAllByOwner(@RequestHeader("X-Sharer-User-Id") int ownerId) {
        return itemService.getAllByOwner(ownerId).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemService.search(text).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }
}
