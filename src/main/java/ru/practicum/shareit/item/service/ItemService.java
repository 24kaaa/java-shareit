package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    Item create(Item item, Long ownerId);

    Item update(Long itemId, Item updateData, Long ownerId);

    Item getById(Long itemId);

    List<Item> getAllByOwner(Long ownerId);

    List<Item> search(String text);

    ItemWithBookingsDto getItemWithBookings(Long itemId, Long userId);

    List<ItemWithBookingsDto> getAllItemsByOwner(Long userId);

    CommentDto addComment(Long itemId, CommentDto commentDto, Long userId);
}
