package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        User user = UserMapper.toEntity(userDto);
        return UserMapper.toDto(userService.create(user));
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable int userId,@Valid @RequestBody UserDto userDto) {
        User user = UserMapper.toEntity(userDto);
        return UserMapper.toDto(userService.update(userId, user));
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable int userId) {
        return UserMapper.toDto(userService.getById(userId));
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable int userId) {
        userService.delete(userId);
    }
}
