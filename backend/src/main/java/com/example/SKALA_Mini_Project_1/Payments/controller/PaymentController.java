package com.example.SKALA_Mini_Project_1.Payments.controller;

import com.example.SKALA_Mini_Project_1.Payments.controller.dto.PaymentSubmitResponse;
import com.example.SKALA_Mini_Project_1.Payments.service.PaymentService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{paymentId}/submit")
    public ResponseEntity<PaymentSubmitResponse> submit(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.submit(paymentId));
    }
}
