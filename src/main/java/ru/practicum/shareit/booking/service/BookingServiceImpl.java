package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto create(BookingDto bookingDto, Long userId) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Владелец не может бронировать свою вещь");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        validateBookingTime(booking);

        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto approve(Long bookingId, Long userId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Подтверждать бронирование может только владелец");
        }

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException("Бронирование уже было подтверждено или отклонено");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingResponseDto(updatedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDto getById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Просматривать бронирование может только автор или владелец");
        }

        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllByBooker(Long userId, String state, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (from < 0 || size <= 0) {
            throw new ValidationException("Некорректные параметры пагинации");
        }

        Pageable page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
        LocalDateTime now = LocalDateTime.now();

        switch (state.toUpperCase()) {
            case "ALL":
                return bookingRepository.findByBookerId(userId, page)
                        .stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "CURRENT":
                return bookingRepository.findByBookerIdAndStartBeforeAndEndAfter(
                                userId, now, now, page)
                        .stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findByBookerIdAndEndBefore(
                                userId, now, page)
                        .stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.findByBookerIdAndStartAfter(
                                userId, now, page)
                        .stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository.findByBookerIdAndStatus(
                                userId, BookingStatus.WAITING, page)
                        .stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository.findByBookerIdAndStatus(
                                userId, BookingStatus.REJECTED, page)
                        .stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllByOwner(Long userId, String state, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        if (itemRepository.findByOwnerId(userId).isEmpty()) {
            throw new NotFoundException("У пользователя нет вещей для бронирования");
        }

        PageRequest page = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case "ALL":
                return bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, page)
                        .stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "CURRENT":
                return bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                                userId, now, now, page)
                        .stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "PAST":
                return bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now, page)
                        .stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now, page)
                        .stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "WAITING":
                return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                                userId, BookingStatus.WAITING, page)
                        .stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                                userId, BookingStatus.REJECTED, page)
                        .stream()
                        .map(BookingMapper::toBookingResponseDto)
                        .collect(Collectors.toList());
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }

    private void validateBookingTime(Booking booking) {
        if (booking.getStart().isAfter(booking.getEnd())) {
            throw new ValidationException("Дата начала бронирования должна быть раньше даты окончания");
        }

        if (booking.getStart().isEqual(booking.getEnd())) {
            throw new ValidationException("Даты начала и окончания бронирования не могут совпадать");
        }
    }
}
