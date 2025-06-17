package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import java.util.List;

public interface BookingService {
    BookingResponseDto create(BookingDto bookingDto, Long userId);

    BookingResponseDto approve(Long bookingId, Long userId, Boolean approved);

    BookingResponseDto getById(Long bookingId, Long userId);

    List<BookingResponseDto> getAllByBooker(Long userId, String state, int from, int size);

    List<BookingResponseDto> getAllByOwner(Long userId, String state, int from, int size);
}
