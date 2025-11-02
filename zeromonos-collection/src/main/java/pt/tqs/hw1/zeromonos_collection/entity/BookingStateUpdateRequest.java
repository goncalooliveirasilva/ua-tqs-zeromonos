package pt.tqs.hw1.zeromonos_collection.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingStateUpdateRequest {
    private State state;
}
