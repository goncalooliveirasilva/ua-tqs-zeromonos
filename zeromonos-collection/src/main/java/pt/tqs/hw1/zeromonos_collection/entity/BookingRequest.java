package pt.tqs.hw1.zeromonos_collection.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private String municipality;
    private LocalDate date;
    private LocalTime time;
    private String description;
}
