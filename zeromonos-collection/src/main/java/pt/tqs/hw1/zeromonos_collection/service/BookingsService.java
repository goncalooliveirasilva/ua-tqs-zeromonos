package pt.tqs.hw1.zeromonos_collection.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pt.tqs.hw1.zeromonos_collection.entity.Booking;
import pt.tqs.hw1.zeromonos_collection.entity.BookingRequest;
import pt.tqs.hw1.zeromonos_collection.entity.State;
import pt.tqs.hw1.zeromonos_collection.repository.BookingRepository;

@Service
@RequiredArgsConstructor
public class BookingsService {
    private final BookingRepository bookingRepository;

    // max capacity for municipality per day (all equal to simplify)
    private final Integer maxCapacityPerDay = 20;

    public Booking createBooking(BookingRequest request, String createdBy) {

        // deny booking for past dates
        if (request.getDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot booking for past dates.");
        }

        // deny booking on weekends
        if (isWeekend(request.getDate())) {
            throw new IllegalStateException("Bookings are not possible on weeknds.");
        }

        Long currentCount = bookingRepository.countByMunicipalityAndDate(
            request.getMunicipality(),
            request.getDate()
        );

        // deny bookings that reached max capacity
        if (currentCount >= maxCapacityPerDay) {
            throw new IllegalStateException("Booking capacity reached for that date in this municipality.");
        }


        Booking booking = Booking.builder()
            .municipality(request.getMunicipality())
            .date(request.getDate())
            .description(request.getDescription())
            .state(State.RECEIVED)
            .token(generateToken())
            .createdBy(createdBy)
            .build();
        return bookingRepository.save(booking);
    }

    public Optional<Booking> getByToken(String token) {
        return bookingRepository.findByToken(token);
    }

    public List<Booking> getBookingsByCitizen(String userEmail) {
        return bookingRepository.findAllByCreatedBy(userEmail);
    }

    public Booking cancelBooking(Long id) {
        Booking booking = getById(id);
        booking.setState(State.CANCELED);
        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getById(Long id) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Booking not found."));
        return booking;
    }

    public Booking updateState(Long id, State newState) {
        Booking booking = getById(id);
        State currentState = booking.getState();

        if (newState == currentState) {
            return booking;
        }

        if (!currentState.canTransitionTo(newState)) {
            throw new IllegalStateException("Invalid state transition: " + currentState + " to " + newState);
        }

        booking.setState(newState);
        return bookingRepository.save(booking);
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
