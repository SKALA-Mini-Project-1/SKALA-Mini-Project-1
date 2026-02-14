package com.example.SKALA_Mini_Project_1.modules.seats.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.SKALA_Mini_Project_1.modules.seats.domain.Seat;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    @Query(
            value = """
                    SELECT s.id, s.section, s.row_number, s.seat_number, s.status, s.grade, s.price
                    FROM seats s
                    JOIN schedules sc ON sc.id = s.schedule_id
                    WHERE sc.concert_id = :concertId
                    ORDER BY s.section, s.row_number, s.seat_number
                    """,
            nativeQuery = true
    )
    List<Object[]> findSeatMapByConcertId(Long concertId);
}
