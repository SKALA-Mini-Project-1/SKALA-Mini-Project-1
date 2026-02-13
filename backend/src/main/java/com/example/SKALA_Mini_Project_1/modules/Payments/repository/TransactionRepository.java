package com.example.SKALA_Mini_Project_1.modules.Payments.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.SKALA_Mini_Project_1.modules.Payments.domain.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
