package pt.tqs.hw1.zeromonos_collection.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.tqs.hw1.zeromonos_collection.entity.BookingStateHistory;

public interface BookingStateHistoryRepository extends JpaRepository<BookingStateHistory, Long> {
    List<BookingStateHistory> findByBookingIdOrderByTimestampAsc(Long bookingId);
}
