package com.example.SKALA_Mini_Project_1.Payments.repository;

import com.example.SKALA_Mini_Project_1.Payments.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
