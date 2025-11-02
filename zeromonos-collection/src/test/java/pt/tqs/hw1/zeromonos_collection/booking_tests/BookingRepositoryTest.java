package pt.tqs.hw1.zeromonos_collection.booking_tests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import pt.tqs.hw1.zeromonos_collection.entity.Booking;
import pt.tqs.hw1.zeromonos_collection.entity.State;
import pt.tqs.hw1.zeromonos_collection.repository.BookingRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DataJpaTest
public class BookingRepositoryTest {
    
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Count bookings for a municipality on a specific date")    
    void testCountByMunicipalityAndDate() {
        LocalDate date = LocalDate.of(2025, 1, 1);

        bookingRepository.save(Booking.builder()
            .municipality("Lisbon")
            .village("Sintra")
            .postalCode("1111-111")
            .date(date)
            .time(LocalTime.now())
            .description("item 1")
            .state(State.RECEIVED)
            .token("t1")
            .createdBy("bob@email.com")
            .build());

        bookingRepository.save(Booking.builder()
            .municipality("Lisbon")
            .village("Sintra")
            .postalCode("0000-000")
            .date(date)
            .time(LocalTime.now())
            .description("item 2")
            .state(State.RECEIVED)
            .token("t2")
            .createdBy("bob@email.com")
            .build());

        Long count = bookingRepository.countByMunicipalityAndDate("Lisbon", date);
        assertThat(count).isEqualTo(2);
    }
}
