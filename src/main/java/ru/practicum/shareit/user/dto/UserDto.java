package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;

    @Size(max = 255, message = "Имя слишком длинное")
    private String name;

    @Email(message = "Некорректный формат email")
    private String email;
}
