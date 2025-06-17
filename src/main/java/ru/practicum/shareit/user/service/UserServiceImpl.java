package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Пользователь с email " + user.getEmail() + " уже существует");
        }
        return userRepository.save(user);
    }

    @Override
    public User update(Long userId, User user) {
        User existingUser = getById(userId);
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new ConflictException("Email " + user.getEmail() + " уже используется");
            }
            existingUser.setEmail(user.getEmail());
        }
        return userRepository.save(existingUser);
    }

    @Override
    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }
}
