package ru.practicum.shareit.user.service;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.InternalServerException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<String, User> emailToUser = new HashMap<>();
    private int idCounter = 1;

    @Override
    public User create(User user) {
            if (emailToUser.containsKey(user.getEmail())) {
                throw new ConflictException("Email " + user.getEmail() + " используется");
            }

            if (user.getEmail() == null) {
                throw new InternalServerException("Опа");
            }

            user.setId(idCounter++);
            users.put(user.getId(), user);
            emailToUser.put(user.getEmail(), user);
            return user;
    }

    @Override
    public User update(int userId,@Valid User user) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        if (user.getEmail() != null && emailToUser.containsKey(user.getEmail())) {
            throw new ConflictException("Email " + user.getEmail() + " используется");
        }

        user.setId(userId);
        users.put(userId, user);
        return user;
    }

    @Override
    public User getById(int userId) throws NotFoundException {
        return Optional.ofNullable(users.get(userId))
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(int userId) {
        users.remove(userId);
    }
}
