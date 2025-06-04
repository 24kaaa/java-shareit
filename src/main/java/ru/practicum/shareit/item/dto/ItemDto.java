package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private int id;

    @NotBlank(message = "Название не может быть пустым")
    @Size(max = 255, message = "Название слишком длинное")
    private String name;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 1000, message = "Описание слишком длинное")
    private String description;

    @NotNull(message = "Статус доступности не может быть null")
    private Boolean available;

    private ItemRequestDto request;
    private Integer requestId;
}
