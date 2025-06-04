package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.User;

import java.util.List;

public interface UserService {

    User create(User user);

    User update(int userId, User user);

    User getById(int userId);

    List<User> getAll();

    void delete(int userId);
}
