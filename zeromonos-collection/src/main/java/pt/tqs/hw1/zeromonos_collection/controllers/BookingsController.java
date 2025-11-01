package pt.tqs.hw1.zeromonos_collection.controllers;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
import pt.tqs.hw1.zeromonos_collection.entity.Booking;
import pt.tqs.hw1.zeromonos_collection.entity.BookingRequest;
import pt.tqs.hw1.zeromonos_collection.entity.BookingStateUpdateRequest;
import pt.tqs.hw1.zeromonos_collection.service.BookingsService;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingsController {
    
    private final BookingsService bookingsService;

    @GetMapping("/public/{token}")
    public ResponseEntity<Booking> getBookingByToken(@PathVariable String token) {
        Optional<Booking> booking = bookingsService.getByToken(token);
        if (booking.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(booking.get());
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest request, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            Booking booking = bookingsService.createBooking(request, userEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/me")
    public ResponseEntity<List<Booking>> getAllBookingsForCitizen(Authentication authentication) {
        String userEmail = authentication.getName();
        List<Booking> bookings = bookingsService.getBookingsByCitizen(userEmail);
        return ResponseEntity.ok(bookings);
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        try {
            Booking canceled = bookingsService.cancelBooking(id);
            return ResponseEntity.ok(canceled);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingsService.getAllBookings());
    }


    @PreAuthorize("hasRole('CITIZEN') or hasRole('STAFF')")
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingDetails(@RequestParam Long id) {
        try {
            Booking booking = bookingsService.getById(id);
            return ResponseEntity.ok(booking);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/state")
    public ResponseEntity<Booking> updateBookingState(@PathVariable Long id, @RequestBody BookingStateUpdateRequest request) {
        try {
            Booking updated = bookingsService.updateState(id, request.getState());
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }   


}
