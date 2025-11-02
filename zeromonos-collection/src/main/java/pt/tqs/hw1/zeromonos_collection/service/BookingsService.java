package pt.tqs.hw1.zeromonos_collection.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pt.tqs.hw1.zeromonos_collection.entity.Booking;
import pt.tqs.hw1.zeromonos_collection.entity.BookingRequest;
import pt.tqs.hw1.zeromonos_collection.entity.BookingStateHistory;
import pt.tqs.hw1.zeromonos_collection.entity.State;
import pt.tqs.hw1.zeromonos_collection.repository.BookingRepository;
import pt.tqs.hw1.zeromonos_collection.repository.BookingStateHistoryRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingsService {

    private final BookingRepository bookingRepository;
    private final BookingStateHistoryRepository bookingStateHistoryRepository;

    // max capacity for municipality per day (all equal to simplify)
    private final Integer MAX_CAPACITY_PER_DAY_PER_MUNICIPALITY = 10;


    public Booking createBooking(BookingRequest request, String createdBy) {
        log.info("Booking requested for email={}", createdBy);

        // deny booking for past dates
        if (request.getDate().isBefore(LocalDate.now())) {
            log.warn("Booking request rejected: booking for past date.");
            throw new IllegalArgumentException("Cannot booking for past dates.");
        }

        // deny booking for out of service hours
        if (request.getTime().isBefore(LocalTime.of(8, 0)) || request.getTime().isAfter(LocalTime.of(17, 0))) {
            throw new IllegalArgumentException("Bookings allowed only between 08:00 and 17:00.");
        }

        // deny booking on weekends
        if (isWeekend(request.getDate())) {
            log.warn("Booking request rejected: booking for weekends.");
            throw new IllegalArgumentException("Bookings are not possible on weekends.");
        }

        Long currentCount = bookingRepository.countByMunicipalityAndDate(
            request.getMunicipality(),
            request.getDate()
        );

        // deny bookings that reached max capacity
        if (currentCount >= MAX_CAPACITY_PER_DAY_PER_MUNICIPALITY) {
            log.warn("Booking request rejected: booking capacity reached for that date.");
            throw new IllegalArgumentException("Daily capacity reached for " + request.getMunicipality());
        }

        Booking booking = Booking.builder()
            .municipality(request.getMunicipality())
            .village(request.getVillage())
            .postalCode(request.getPostalCode())
            .date(request.getDate())
            .description(request.getDescription())
            .time(request.getTime())
            .state(State.RECEIVED)
            .token(generateToken())
            .createdBy(createdBy)
            .build();
        
        log.info("Booking created for email={}", createdBy);
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
        State currentState = booking.getState();
        if (!currentState.canTransitionTo(State.CANCELED)) {
            log.warn("Booking cancelation rejected: invalid state transition");
            throw new IllegalStateException("Invalid state transition: " + currentState + " to CANCELED");
        }
        booking.setState(State.CANCELED);

        log.info("Booking canceled for email={}", booking.getCreatedBy());
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

    public Booking updateState(Long id, State newState, String changedBy) {
        log.info("Booking state update requested with new_state={}; for email={}", newState, changedBy);
        Booking booking = getById(id);
        State currentState = booking.getState();

        if (newState == currentState) {
            return booking;
        }

        if (!currentState.canTransitionTo(newState)) {
            log.warn("Booking state update rejected: invalid state transition; for email={}", changedBy);
            throw new IllegalStateException("Invalid state transition: " + currentState + " to " + newState);
        }

        booking.setState(newState);
        Booking saved = bookingRepository.save(booking);
        BookingStateHistory history = BookingStateHistory.builder()
            .bookingId(saved.getId().longValue())
            .state(newState)
            .timestamp(LocalDateTime.now())
            .changedBy(changedBy)
            .build();
        
        log.info("Booking state updated: from={} to={}", currentState, newState);
        bookingStateHistoryRepository.save(history);
        return saved;
    }

    public List<BookingStateHistory> getHistoryForBooking(Long id) {
        log.info("Booking history requested");
        return bookingStateHistoryRepository.findByBookingIdOrderByTimestampAsc(id);
    }

    public List<LocalTime> getAvailableTimes(String municipality, LocalDate date) {
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(17, 0);

        List<LocalTime> allSlots = new ArrayList<>();
        for (LocalTime t = start; t.isBefore(end); t = t.plusHours(1)) {
            allSlots.add(t);
        }

        List<Booking> bookings = bookingRepository.findByMunicipalityAndDate(municipality, date);
        Set<LocalTime> bookedTimes = bookings.stream()
            .map(Booking::getTime)
            .collect(Collectors.toSet());
        
        return allSlots.stream()
            .filter(t -> !bookedTimes.contains(t))
            .collect(Collectors.toList());
    }

    public List<Booking> getBookingsByMunicipality(String municipality) {
        log.info("Bookings requested for municipality={}", municipality);
        return bookingRepository.findByMunicipality(municipality);
    }

    private String generateToken() {
        return UUID.randomUUID().toString();
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
