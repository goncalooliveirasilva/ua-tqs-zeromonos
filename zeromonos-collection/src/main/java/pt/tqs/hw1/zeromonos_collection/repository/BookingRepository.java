package pt.tqs.hw1.zeromonos_collection.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pt.tqs.hw1.zeromonos_collection.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByToken(String token);
    List<Booking> findAllByCreatedBy(String userEmail);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.municipality = :municipality AND b.date = :date")
    Long countByMunicipalityAndDate(@Param("municipality") String municipality, @Param("date") LocalDate date);
}
