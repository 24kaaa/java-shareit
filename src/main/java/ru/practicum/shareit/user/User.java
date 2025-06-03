package ru.practicum.shareit.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class User {
    private int id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;
}
