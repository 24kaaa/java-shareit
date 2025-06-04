package ru.practicum.shareit.booking;

import lombok.Data;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
public class Booking {
    private int id;
    private LocalDateTime start; // Дата и время начала бронирования
    private LocalDateTime end; // Дата и время окончания бронирования
    private Item item; // Бронируемая вещь
    private User booker; // Пользователь, который бронирует
    private Status status; // Статус бронирования

    public enum Status {
        WAITING,   // Ожидает подтверждения
        APPROVED,  // Подтверждено владельцем
        REJECTED,   // Отклонено владельцем
        CANCELED    // Отменено создателем
    }
}
