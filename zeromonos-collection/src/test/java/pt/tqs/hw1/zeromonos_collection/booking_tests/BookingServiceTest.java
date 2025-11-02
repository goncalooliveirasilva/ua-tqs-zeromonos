package pt.tqs.hw1.zeromonos_collection.booking_tests;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.tqs.hw1.zeromonos_collection.entity.Booking;
import pt.tqs.hw1.zeromonos_collection.entity.BookingRequest;
import pt.tqs.hw1.zeromonos_collection.entity.State;
import pt.tqs.hw1.zeromonos_collection.repository.BookingRepository;
import pt.tqs.hw1.zeromonos_collection.repository.BookingStateHistoryRepository;
import pt.tqs.hw1.zeromonos_collection.service.BookingsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingStateHistoryRepository bookingStateHistoryRepository;

    @InjectMocks
    private BookingsService bookingsService;


    @Test
    @DisplayName("Create booking")
    void testCreateBooking() {
        BookingRequest request = BookingRequest.builder()
            .municipality("Lisbon")
            .village("Sintra")
            .postalCode("0000-000")
            .date(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)))
            .time(LocalTime.of(10, 0))
            .description("item 1")
            .build();
        
        Booking saved = Booking.builder()
            .id(1L)
            .municipality(request.getMunicipality())
            .village("Sintra")
            .postalCode("0000-000")
            .date(request.getDate())
            .time(request.getTime())
            .description(request.getDescription())
            .state(State.RECEIVED)
            .createdBy("bob@email.com")
            .token("token")
            .build();

        when(bookingRepository.countByMunicipalityAndDate(anyString(), any())).thenReturn(0L);
        when(bookingRepository.save(any())).thenReturn(saved);

        Booking result = bookingsService.createBooking(request, "bob@email.com");

        assertThat(result).isNotNull();
        assertThat(result.getState()).isEqualTo(State.RECEIVED);
        assertThat(result.getMunicipality()).isEqualTo("Lisbon");
        verify(bookingRepository, times(1)).save(any());
    }


    @Test
    @DisplayName("Deny booking when max capacity is reached")
    void testCreateBookingMaxCapacityReached() {
        BookingRequest request = BookingRequest.builder()
            .municipality("Lisbon")
            .village("Sintra")
            .postalCode("0000-000")
            .date(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)))
            .time(LocalTime.of(10, 0))
            .description("item 1")
            .build();
        
        // simulate max capacity
        when(bookingRepository.countByMunicipalityAndDate("Lisbon", request.getDate())).thenReturn(20L);

        Exception e = assertThrows(IllegalArgumentException.class, () -> bookingsService.createBooking(request, "bob@email.com"));

        assertThat(e.getMessage()).isEqualTo("Daily capacity reached for Lisbon");
    }


    @Test
    @DisplayName("Allow booking exactly at 08:00 and 17:00")
    void testCreateBookingArEdgeHours() {
        BookingRequest b1 = BookingRequest.builder()
            .municipality("Lisbon")
            .village("Sintra")
            .postalCode("0000-000")
            .date(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)))
            .time(LocalTime.of(8, 0))
            .description("item 1")
            .build();
        BookingRequest b2 = BookingRequest.builder()
            .municipality("Lisbon")
            .village("Sintra")
            .postalCode("0000-000")
            .date(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)))
            .time(LocalTime.of(17, 0))
            .description("item 1")
            .build();

        Booking saved = Booking.builder()
            .id(1L)
            .municipality("Lisbon")
            .village("Sintra")
            .postalCode("0000-000")
            .date(b1.getDate())
            .time(b1.getTime())
            .description(b1.getDescription())
            .state(State.RECEIVED)
            .createdBy("bob@email.com")
            .token("token")
            .build();

        when(bookingRepository.countByMunicipalityAndDate(anyString(), any())).thenReturn(0L);
        when(bookingRepository.save(any())).thenReturn(saved);

        Booking result1 = bookingsService.createBooking(b1, "bob@email.com");
        Booking result2 = bookingsService.createBooking(b2, "bob@email.com");

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
    }


    @Test
    @DisplayName("Deny booking in the past")
    void testBookingForPastDate() {
        BookingRequest request = BookingRequest.builder()
            .municipality("Lisbon")
            .village("Sintra")
            .postalCode("0000-000")
            .date(LocalDate.now().with(TemporalAdjusters.previous(DayOfWeek.MONDAY)))
            .time(LocalTime.of(10, 0))
            .description("item 1")
            .build();
    
        Exception e = assertThrows(IllegalArgumentException.class, () -> bookingsService.createBooking(request, "bob@email.com"));
        assertThat(e.getMessage()).isEqualTo("Cannot booking for past dates.");
    }


    @Test
    @DisplayName("Deny booking outside of working hours")
    void testCreateBookingOutOfHours() {
        BookingRequest request = BookingRequest.builder()
            .municipality("Lisbon")
            .village("Sintra")
            .postalCode("0000-000")
            .date(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)))
            .time(LocalTime.of(7, 0))
            .description("item 1")
            .build();
        
        Exception e = assertThrows(IllegalArgumentException.class, () -> bookingsService.createBooking(request, "bob@email.com"));
        assertThat(e.getMessage()).isEqualTo("Bookings allowed only between 08:00 and 17:00.");
    }


    @Test
    @DisplayName("Deny bookings on weekends")
    void testCreateBookingsOnWeekends() {
        BookingRequest request = BookingRequest.builder()
            .municipality("Lisbon")
            .village("Sintra")
            .postalCode("0000-000")
            .date(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SATURDAY)))
            .time(LocalTime.of(10, 0))
            .description("item 1")
            .build();

        Exception e = assertThrows(IllegalArgumentException.class, () -> bookingsService.createBooking(request, "bob@email.com"));
        assertThat(e.getMessage()).isEqualTo("Bookings are not possible on weekends.");
    }


    @Test
    @DisplayName("Cancel booking")
    void testCancelBooking() {
        Booking booking = Booking.builder()
            .id(1L)
            .municipality("Lisbon")
            .village("Sintra")
            .postalCode("0000-000")
            .date(LocalDate.now())
            .time(LocalTime.now())
            .description("item 1")
            .state(State.RECEIVED)
            .createdBy("bob@email.com")
            .token("token")
            .build();
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        Booking canceled = bookingsService.cancelBooking(booking.getId());

        assertThat(canceled.getState()).isEqualTo(State.CANCELED);
        verify(bookingRepository, times(1)).save(booking);
    }


    @Test
    @DisplayName("Update booking state")
    void testUpdateState() {
        Booking booking = Booking.builder()
            .id(1L)
            .municipality("Lisbon")
            .village("Sintra")
            .postalCode("0000-000")
            .date(LocalDate.now())
            .time(LocalTime.now())
            .description("item 1")
            .state(State.RECEIVED)
            .createdBy("bob@email.com")
            .token("token")
            .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);
        when(bookingStateHistoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Booking updated = bookingsService.updateState(1L, State.ASSIGNED, "staff@email.com");

        assertThat(updated.getState()).isEqualTo(State.ASSIGNED);
        verify(bookingStateHistoryRepository, times(1)).save(any());
    }


    @Test
    @DisplayName("Deny invalid state transition")
    void testUpdateInvalidStateTransition() {
        Booking booking = Booking.builder()
            .id(1L)
            .municipality("Lisbon")
            .village("Sintra")
            .postalCode("0000-000")
            .date(LocalDate.now())
            .time(LocalTime.now())
            .description("item 1")
            .state(State.RECEIVED)
            .createdBy("bob@email.com")
            .token("token")
            .build();
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Exception e = assertThrows(IllegalStateException.class, () -> bookingsService.updateState(1L, State.DONE, "staff@email.com"));
        assertThat(e.getMessage()).isEqualTo("Invalid state transition: RECEIVED to DONE");
        verify(bookingRepository, never()).save(any());
        verify(bookingStateHistoryRepository, never()).save(any());
    }


    @Test
    @DisplayName("Do nothing when updating to the same state")
    void testUpdateSameState() {
        Booking booking = Booking.builder()
            .id(1L)
            .municipality("Lisbon")
            .village("Sintra")
            .postalCode("0000-000")
            .date(LocalDate.now())
            .time(LocalTime.now())
            .description("item 1")
            .state(State.RECEIVED)
            .createdBy("bob@email.com")
            .token("token")
            .build();
        
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        Booking result = bookingsService.updateState(1L, State.RECEIVED, "staff@email.com");

        assertThat(result).isSameAs(booking);
        assertThat(result.getState()).isEqualTo(State.RECEIVED);
        verify(bookingRepository, never()).save(any());
        verify(bookingStateHistoryRepository, never()).save(any());
    }


    @Test
    @DisplayName("Get available times for booking")
    void testGetAvailableTimes() {
        LocalDate date = LocalDate.now().plusDays(1);
        List<Booking> bookings = Arrays.asList(
            Booking.builder().time(LocalTime.of(9, 0)).build(),
            Booking.builder().time(LocalTime.of(12, 0)).build()
        );

        when(bookingRepository.findByMunicipalityAndDate("Lisbon", date)).thenReturn(bookings);

        List<LocalTime> available = bookingsService.getAvailableTimes("Lisbon", date);

        assertThat(available).doesNotContain(LocalTime.of(9, 0), LocalTime.of(12, 0));
        assertThat(available).contains(LocalTime.of(8, 0), LocalTime.of(10, 0), LocalTime.of(11, 0), LocalTime.of(13, 0));
    }
}
