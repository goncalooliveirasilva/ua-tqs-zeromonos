package pt.tqs.hw1.zeromonos_collection.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pt.tqs.hw1.zeromonos_collection.entity.Booking;
import pt.tqs.hw1.zeromonos_collection.entity.BookingRequest;
import pt.tqs.hw1.zeromonos_collection.entity.BookingStateHistory;
import pt.tqs.hw1.zeromonos_collection.entity.BookingStateUpdateRequest;
import pt.tqs.hw1.zeromonos_collection.service.BookingsService;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingsController {
    
    private final BookingsService bookingsService;

    @GetMapping("/public/{token}")
    public ResponseEntity<Booking> getBookingByToken(@PathVariable String token) {
        Optional<Booking> booking = bookingsService.getByToken(token);

        if (booking.isEmpty()) {
            log.info("GET booking by token not found for token={}", token);
            return ResponseEntity.notFound().build();
        }

        log.info("GET booking by token for token={}", token);
        return ResponseEntity.ok(booking.get());
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        Booking booking = bookingsService.createBooking(request, userEmail);

        log.info("POST create a booking for email={}", userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/me")
    public ResponseEntity<List<Booking>> getAllBookingsForCitizen(Authentication authentication) {
        String userEmail = authentication.getName();
        List<Booking> bookings = bookingsService.getBookingsByCitizen(userEmail);

        log.info("GET bookings for citizen={}", userEmail);
        return ResponseEntity.ok(bookings);
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long id) {
        Booking canceled = bookingsService.cancelBooking(id);

        log.info("DELETE cancel booking={}", id);
        return ResponseEntity.ok(canceled);
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {

        log.info("GET all bookings");
        return ResponseEntity.ok(bookingsService.getAllBookings());
    }


    @PreAuthorize("hasRole('CITIZEN') or hasRole('STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingDetails(@PathVariable Long id) {
        Booking booking = bookingsService.getById(id);

        log.info("GET details for booking={}", id);
        return ResponseEntity.ok(booking);
    }

    @PreAuthorize("hasRole('STAFF')")
    @PutMapping("/{id}/state")
    public ResponseEntity<Booking> updateBookingState(@PathVariable Long id, @RequestBody BookingStateUpdateRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        Booking updated = bookingsService.updateState(id, request.getState(), userEmail);

        log.info("PUT update state for booking={} for email={}", userEmail);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('CITIZEN') or hasRole('STAFF')")
    @GetMapping("/{id}/history")
    public ResponseEntity<List<BookingStateHistory>> getBookingHistory(@PathVariable Long id) {
        log.info("GET history for booking={}", id);
        return ResponseEntity.ok(bookingsService.getHistoryForBooking(id));
    }

    @PreAuthorize("hasRole('CITIZEN') or hasRole('STAFF')")
    @GetMapping("/available-times")
    public ResponseEntity<List<LocalTime>> getAvailableTimes(
        @RequestParam String municipality,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<LocalTime> available = bookingsService.getAvailableTimes(municipality, date);
        log.info("GET available times for municipality={} and date={}", municipality, date);
        return ResponseEntity.ok(available);
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/municipality/{municipality}")
    public ResponseEntity<List<Booking>> getBookingsByMunicipality(@PathVariable String municipality) {
        List<Booking> bookings = bookingsService.getBookingsByMunicipality(municipality);
        log.info("GET bookings for municipality={}", municipality);
        return ResponseEntity.ok(bookings);
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/district/{district}")
    public ResponseEntity<List<Booking>> getBookingsByDistrict(@PathVariable String district) {
        List<Booking> bookings = bookingsService.getBookingsByDistrict(district);
        log.info("GET bookings for district={}", district);
        return ResponseEntity.ok(bookings);
    }
}
