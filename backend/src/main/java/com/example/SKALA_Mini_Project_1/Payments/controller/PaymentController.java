package com.example.SKALA_Mini_Project_1.Payments.controller;

import com.example.SKALA_Mini_Project_1.Payments.controller.dto.PaymentCreateRequest;
import com.example.SKALA_Mini_Project_1.Payments.controller.dto.PaymentCreateResponse;
import com.example.SKALA_Mini_Project_1.Payments.controller.dto.PaymentGetResponse;
import com.example.SKALA_Mini_Project_1.Payments.controller.dto.PaymentSubmitResponse;
import com.example.SKALA_Mini_Project_1.Payments.service.PaymentService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;


    @PostMapping("/create")
    public ResponseEntity<PaymentCreateResponse> create(@RequestBody PaymentCreateRequest req) {
        return ResponseEntity.ok(paymentService.createPayment(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentGetResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @PostMapping("/{paymentId}/submit")
    public ResponseEntity<PaymentSubmitResponse> submit(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.submit(paymentId));
    }
}
