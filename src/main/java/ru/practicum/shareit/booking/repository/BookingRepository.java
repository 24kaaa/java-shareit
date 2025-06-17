package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByBookerId(Long bookerId, Pageable pageable);

    Page<Booking> findByBookerIdAndStartBeforeAndEndAfter(
            Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Booking> findByBookerIdAndEndBefore(
            Long bookerId, LocalDateTime end, Pageable pageable);

    Page<Booking> findByBookerIdAndStartAfter(
            Long bookerId, LocalDateTime start, Pageable pageable);

    Page<Booking> findByBookerIdAndStatus(
            Long bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(
            Long ownerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(
            Long ownerId, BookingStatus status, Pageable pageable);

    Booking findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
            Long itemId, LocalDateTime now, BookingStatus status);

    Booking findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
            Long itemId, LocalDateTime now, BookingStatus status);

    boolean existsByBookerIdAndItemIdAndEndBefore(Long bookerId, Long itemId, LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start ASC")
    List<Booking> findApprovedBookingsForItem(@Param("itemId") Long itemId);
}
