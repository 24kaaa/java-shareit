package ru.practicum.shareit.item.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public Item create(Item item, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Владелец с id=" + ownerId + " не найден"));

        if (item.getAvailable() == null) {
            throw new ValidationException("Поле available обязательно");
        }

        item.setOwner(owner);
        return itemRepository.save(item);
    }

    @Override
    public Item update(Long itemId, Item updateData, Long ownerId) {
        Item existingItem = getById(itemId);

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Редактировать может только владелец");
        }

        if (updateData.getName() != null) {
            existingItem.setName(updateData.getName());
        }
        if (updateData.getDescription() != null) {
            existingItem.setDescription(updateData.getDescription());
        }
        if (updateData.getAvailable() != null) {
            existingItem.setAvailable(updateData.getAvailable());
        }

        return itemRepository.save(existingItem);
    }

    @Override
    public Item getById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
    }

    @Override
    public List<Item> getAllByOwner(Long ownerId) {
        return itemRepository.findByOwnerId(ownerId);
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text);
    }

    @Override
    public List<ItemWithBookingsDto> getAllItemsByOwner(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Item> items = itemRepository.findByOwnerId(userId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    List<Booking> bookings = bookingRepository.findApprovedBookingsForItem(item.getId());

                    BookingShortDto lastBooking = bookings.stream()
                            .filter(b -> b.getEnd().isBefore(now))
                            .max(Comparator.comparing(Booking::getEnd))
                            .map(booking -> BookingShortDto.builder()
                                    .id(booking.getId())
                                    .bookerId(booking.getBooker().getId())
                                    .start(booking.getStart())
                                    .end(booking.getEnd())
                                    .build())
                            .orElse(null);

                    BookingShortDto nextBooking = bookings.stream()
                            .filter(b -> b.getStart().isAfter(now))
                            .min(Comparator.comparing(Booking::getStart))
                            .map(booking -> BookingShortDto.builder()
                                    .id(booking.getId())
                                    .bookerId(booking.getBooker().getId())
                                    .start(booking.getStart())
                                    .end(booking.getEnd())
                                    .build())
                            .orElse(null);

                    List<CommentDto> comments = commentRepository.findByItemId(item.getId()).stream()
                            .map(CommentMapper::toDto)
                            .collect(Collectors.toList());

                    return ItemWithBookingsDto.builder()
                            .id(item.getId())
                            .name(item.getName())
                            .description(item.getDescription())
                            .available(item.getAvailable())
                            .lastBooking(lastBooking)
                            .nextBooking(nextBooking)
                            .comments(comments)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto addComment(Long itemId, CommentDto commentDto, Long userId) {

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        boolean hasBooked = bookingRepository.existsByBookerIdAndItemIdAndEndBefore(
                userId, itemId, LocalDateTime.now());

        if (!hasBooked) {
            throw new ValidationException("Пользователь не брал вещь в аренду или аренда еще не завершена");
        }

        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        Comment comment = CommentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toDto(savedComment);
    }

    @Override
    public ItemWithBookingsDto getItemWithBookings(Long itemId, Long userId) {
        Item item = getById(itemId);
        List<CommentDto> comments = getCommentsForItem(itemId);

        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            List<Booking> bookings = bookingRepository.findApprovedBookingsForItem(itemId);

            lastBooking = bookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .max(Comparator.comparing(Booking::getEnd))
                    .map(this::convertToShortDto)
                    .orElse(null);

            nextBooking = bookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .min(Comparator.comparing(Booking::getStart))
                    .map(this::convertToShortDto)
                    .orElse(null);
        }

        return ItemMapper.toItemWithBookingsDto(item, lastBooking, nextBooking, comments);
    }

    private List<CommentDto> getCommentsForItem(Long itemId) {
        return commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    private BookingShortDto convertToShortDto(Booking booking) {
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();
    }
}
