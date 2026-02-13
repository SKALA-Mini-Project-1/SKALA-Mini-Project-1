package com.example.SKALA_Mini_Project_1.domain.Seats.repository;

import com.example.SKALA_Mini_Project_1.domain.Seats.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
}
