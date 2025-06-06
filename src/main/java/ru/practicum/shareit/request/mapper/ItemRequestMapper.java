package ru.practicum.shareit.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {
    public static ItemRequestDto toDto(ItemRequest request) {
        return new ItemRequestDto(
                request.getId(),
                request.getDescription(),
                request.getCreated()
        );
    }

    public static ItemRequest toEntity(ItemRequestDto requestDto) {
        ItemRequest request = new ItemRequest();
        request.setId(requestDto.getId());
        request.setDescription(requestDto.getDescription());
        request.setCreated(requestDto.getCreated());
        return request;
    }
}
