package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.User;
import java.util.List;

public interface UserService {

    User create(User user);

    User update(Long userId, User user);

    User getById(Long userId);

    List<User> getAll();

    void delete(Long userId);
}
