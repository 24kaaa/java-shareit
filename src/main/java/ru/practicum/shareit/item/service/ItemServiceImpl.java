package ru.practicum.shareit.item.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private final Map<Integer, Item> items = new HashMap<>();
    private final UserService userService;
    private int idCounter = 1;


    @Autowired
    public ItemServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Item create(Item item, int ownerId) {
        User owner = userService.getById(ownerId);
        item.setOwner(owner);

        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("Поле available обязательно");
        }

        item.setId(idCounter++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(int itemId, Item updateData, int ownerId) {
        Item existingItem = getById(itemId);

        if (existingItem.getOwner().getId() != ownerId) {
            throw new ForbiddenException("Редактировать может только владелец");
        }

        if (updateData.getName() != null) {
            existingItem.setName(updateData.getName());
        }

        if (updateData.getDescription() != null) {
            existingItem.setDescription(updateData.getDescription());
        }

        if (updateData.getAvailable() != null) {
            existingItem.setAvailable(updateData.getAvailable());
        }
        return existingItem;
    }

    @Override
    public Item getById(int itemId) {
        return Optional.ofNullable(items.get(itemId))
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
    }

    @Override
    public List<Item> getAllByOwner(int ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId() == ownerId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String searchText = text.toLowerCase();

        return items.values().stream()
                .filter(item -> item.getAvailable() &&
                        (item.getName().toLowerCase().contains(searchText) ||
                                item.getDescription().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());
    }
}
