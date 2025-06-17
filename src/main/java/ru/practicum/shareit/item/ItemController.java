package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
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
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        Item item = ItemMapper.toEntity(itemDto);
        Item createdItem = itemService.create(item, ownerId);
        return ItemMapper.toDto(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(
            @PathVariable Long itemId,
            @Valid @RequestBody ItemUpdateDto updateDto,
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {

        Item updateData = ItemMapper.toEntity(updateDto);
        Item updatedItem = itemService.update(itemId, updateData, ownerId);
        return ItemMapper.toDto(updatedItem);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsDto getById(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemWithBookings(itemId, userId);
    }

    @GetMapping
    public List<ItemWithBookingsDto> getAllByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.getAllItemsByOwner(ownerId);
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

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable Long itemId,
                                 @RequestBody CommentDto commentDto,
                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.addComment(itemId, commentDto, userId);
    }
}
