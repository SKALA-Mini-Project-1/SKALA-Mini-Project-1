package com.example.SKALA_Mini_Project_1.modules.seats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SKALA_Mini_Project_1.modules.seats.domain.Seat;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
}
