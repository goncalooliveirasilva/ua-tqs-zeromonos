package pt.tqs.hw1.zeromonos_collection.booking_tests;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import pt.tqs.hw1.zeromonos_collection.entity.Booking;
import pt.tqs.hw1.zeromonos_collection.entity.BookingRequest;
import pt.tqs.hw1.zeromonos_collection.entity.BookingStateHistory;
import pt.tqs.hw1.zeromonos_collection.entity.BookingStateUpdateRequest;
import pt.tqs.hw1.zeromonos_collection.entity.State;
import pt.tqs.hw1.zeromonos_collection.repository.BookingRepository;
import pt.tqs.hw1.zeromonos_collection.repository.BookingStateHistoryRepository;

import static org.hamcrest.Matchers.hasSize;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingsControllerTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingStateHistoryRepository bookingStateHistoryRepository;

    private static Booking b;
    private LocalDate futureDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

    @BeforeEach
    @AfterEach
    void cleanDatabase() {
        bookingRepository.deleteAll();
        b = bookingRepository.save(
            Booking.builder()
                .district("District")
                .municipality("Lisbon")
                .village("Sintra")
                .postalCode("0000-000")
                .date(futureDate)
                .time(LocalTime.of(16, 0))
                .description("item 1")
                .state(State.RECEIVED)
                .token("tok1")
                .createdBy("bob@email.com")
                .build()
        );
        bookingStateHistoryRepository.save(
            BookingStateHistory.builder()
                .bookingId(b.getId())
                .state(b.getState())
                .timestamp(LocalDateTime.now())
                .changedBy("staffmember@email.com")
                .build()
        );

    }

    // public endpoint (no authentication required)
    @Test
    @DisplayName("GET /api/v1/bookings/public/{token} returns booking")
    void testGetBookingByToken() throws Exception {
        Booking booking = bookingRepository.save(
            Booking.builder()
                .district("District")
                .municipality("Lisbon")
                .village("Sintra")
                .postalCode("0000-000")
                .date(futureDate)
                .time(LocalTime.of(10, 0))
                .description("item 1")
                .state(State.RECEIVED)
                .token("token")
                .createdBy("bob@email.com")
                .build()
        );

        mockMvc.perform(get("/api/v1/bookings/public/{token}", booking.getToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.municipality").value("Lisbon"))
            .andExpect(jsonPath("$.state").value("RECEIVED"));
    }

    @Test
    @DisplayName("GET /api/v1/bookings/public/{token} returns 404 if not found")
    void testGetBookingByTokenNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/public/{token}", "nonexist"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "bob@email.com", roles = {"CITIZEN"})
    @DisplayName("POST /api/v1/bookings creates a booking successfully")
    void testCreateBooking() throws Exception {
        BookingRequest request = BookingRequest.builder()
            .district("District")
            .municipality("Lisbon")
            .village("Sintra")
            .postalCode("0000-000")
            .date(futureDate)
            .time(LocalTime.of(10, 0))
            .description("item 1")
            .build();

        mockMvc.perform(post("/api/v1/bookings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.municipality").value("Lisbon"))
            .andExpect(jsonPath("$.state").value("RECEIVED"));
    }

    @Test
    @WithMockUser(username = "staff@email.com", roles = {"STAFF"})
    @DisplayName("POST /api/v1/bookings doesn't create a booking")
    void testStaffCreateBooking() throws Exception {
        BookingRequest request = BookingRequest.builder()
            .district("District")
            .municipality("Lisbon")
            .village("Sintra")
            .postalCode("0000-000")
            .date(futureDate)
            .time(LocalTime.of(10, 0))
            .description("item 1")
            .build();
        

        mockMvc.perform(post("/api/v1/bookings")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "bob@email.com", roles = {"CITIZEN"})
    @DisplayName("GET /api/v1/bookings/me returns only citizen's bookings")
    void testGetBookingsForCitizen() throws Exception {
        bookingRepository.save(
            Booking.builder()
                .district("District")
                .municipality("Porto")
                .village("Sintra")
                .postalCode("0000-000")
                .date(LocalDate.now().plusDays(1))
                .time(LocalTime.of(14, 0))
                .description("item 1")
                .state(State.RECEIVED)
                .token("t2")
                .createdBy("other@email.com")
                .build()
        );

        mockMvc.perform(get("/api/v1/bookings/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].municipality").value("Lisbon"))
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "staff@email.com", roles = {"STAFF"})
    @DisplayName("GET /api/v1/bookings/me doesn't work")
    void testGetBookingsForStaff() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "bob@email.com", roles = {"CITIZEN"})
    @DisplayName("DELETE /api/v1/bookings/{id} cancels a booking")
    void testCancelBooking() throws Exception {
        Booking booking = bookingRepository.save(
            Booking.builder()
                .district("District")
                .municipality("Lisbon")
                .village("Sintra")
                .postalCode("0000-000")
                .date(futureDate)
                .time(LocalTime.of(9, 0))
                .description("item 1")
                .state(State.RECEIVED)
                .token("t1")
                .createdBy("bob@email.com")
                .build()
        );

        mockMvc.perform(delete("/api/v1/bookings/{id}", booking.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("CANCELED"));
    }

    @Test
    @WithMockUser(username = "staff@email.com", roles = {"STAFF"})
    @DisplayName("GET /api/v1/bookings return all bookings")
    void testGetAllBookingsStaff() throws Exception {
        mockMvc.perform(get("/api/v1/bookings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].municipality").value("Lisbon"));
    }

    @Test
    @WithMockUser(username = "bob@email.com", roles = {"CITIZEN"})
    @DisplayName("GET /api/v1/bookings/{id} returns booking details for citizen")
    void testGetBookingDetailsForCitizen() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/{id}", b.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.municipality").value("Lisbon"));
    }

    @Test
    @WithMockUser(username = "staff@email.com", roles = {"STAFF"})
    @DisplayName("GET /api/v1/bookings/{id} returns booking details for staff")
    void testGetBookingDetailsForStaff() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/{id}", b.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.municipality").value("Lisbon"));
    }

    @Test
    @WithMockUser(username = "staff@email.com", roles = {"STAFF"})
    @DisplayName("PUT /api/v1/bookings/{id}/state updates booking state")
    void testUpdateBookingStateStaff() throws Exception {
        BookingStateUpdateRequest request = new BookingStateUpdateRequest(State.ASSIGNED);
        mockMvc.perform(put("/api/v1/bookings/{id}/state", b.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("ASSIGNED"));
    }

    @Test
    @WithMockUser(username = "bob@email.com", roles = {"CITIZEN"})
    @DisplayName("PUT /api/v1/bookings/{id}/state updates booking state")
    void testUpdateBookingStateCitizen() throws Exception {
        BookingStateUpdateRequest request = new BookingStateUpdateRequest(State.ASSIGNED);
        mockMvc.perform(put("/api/v1/bookings/{id}/state", b.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(username = "bob@email.com", roles = {"CITIZEN"})
    @DisplayName("GET /api/v1/bookings/{id}/history returns booking history for citizen")
    void testGetBookingHistoryCitizen() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/{id}/history", b.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].timestamp").isNotEmpty());
    }


    @Test
    @WithMockUser(username = "staff@email.com", roles = {"STAFF"})
    @DisplayName("GET /api/v1/bookings/{id}/history returns booking history for staff")
    void testGetBookingHistoryStaff() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/{id}/history", b.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].timestamp").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "bob@email.com", roles = {"CITIZEN"})
    @DisplayName("GET /api/v1/bookings/available-times returns avilable hours")
    void testGetAvailableTimes() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/available-times")
            .param("municipality", "Lisbon")
            .param("date", futureDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "staff@email.com", roles = {"STAFF"})
    @DisplayName("GET /api/v1/bookings/municipality/{municipality} returns bookings for staff")
    void testGetBookingsByMunicipality() throws Exception {
        bookingRepository.save(
            Booking.builder()
                .district("District")
                .municipality("Lisbon")
                .village("Cascais")
                .postalCode("1111-111")
                .date(futureDate.plusDays(1))
                .time(LocalTime.of(10, 0))
                .description("item 2")
                .state(State.ASSIGNED)
                .token("tok2")
                .createdBy("alice@email.com")
                .build()
        );
        
        mockMvc.perform(get("/api/v1/bookings/municipality/{municipality}", "Lisbon"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].municipality").value("Lisbon"))
            .andExpect(jsonPath("$[1].municipality").value("Lisbon"));
    }

    @Test
    @WithMockUser(username = "citizen@email.com", roles = {"CITIZEN"})
    @DisplayName("GET /api/v1/bookings/municipality/{municipality} returns 403 for citizen")
    void testGetBookingsByMunicipalityForbiddenForCitizen() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/municipality/{municipality}", "Lisbon"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/bookings/municipality/{municipality} returns 401 for unauthenticated users")
    void testGetBookingsByMunicipalityUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/municipality/{municipality}", "Lisbon"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "staff@email.com", roles = {"STAFF"})
    @DisplayName("GET /api/v1/bookings/district/{district} returns bookings dor staff")
    void testGetBookingsByDistrict() throws Exception {
        bookingRepository.save(
            Booking.builder()
                .district("Viana")
                .municipality("Ponte de Lima")
                .village("Cascais")
                .postalCode("1111-111")
                .date(futureDate.plusDays(1))
                .time(LocalTime.of(10, 0))
                .description("item 2")
                .state(State.ASSIGNED)
                .token("tok2")
                .createdBy("alice@email.com")
                .build()
        );

        mockMvc.perform(get("/api/v1/bookings/district/{district}", "Viana"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].district").value("Viana"))
            .andExpect(jsonPath("$[0].municipality").value("Ponte de Lima"));
    }

    @Test
    @WithMockUser(username = "citizen@email.com", roles = {"CITIZEN"})
    @DisplayName("GET /api/v1/bookings/district/{district}} returns 403 for citizen")
    void testGetBookingsByDistrictForbiddenForCitizen() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/district/{municipality}", "Viana"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/bookings/district/{district} returns 401 for unauthenticated users")
    void testGetBookingsByDistrictUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/district/{district}", "Viana"))
            .andExpect(status().isForbidden());
    }
}
