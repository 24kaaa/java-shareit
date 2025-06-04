package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    Item create(Item item, int ownerId);

    Item update(int itemId, Item updateData, int ownerId);

    Item getById(int itemId);

    List<Item> getAllByOwner(int ownerId);

    List<Item> search(String text);
}
