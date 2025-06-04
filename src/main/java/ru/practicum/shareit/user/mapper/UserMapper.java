package ru.practicum.shareit.user.mapper;

import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

public class UserMapper {
    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    public static User toEntity(UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        return user;
    }
}
