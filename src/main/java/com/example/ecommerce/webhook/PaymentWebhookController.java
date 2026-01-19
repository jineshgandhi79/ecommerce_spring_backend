package com.example.ecommerce.webhook;

import com.example.ecommerce.dto.PaymentWebhookRequest;
import com.example.ecommerce.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
public class PaymentWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);

    private final PaymentService paymentService;

    public PaymentWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payment")
    public ResponseEntity<Map<String, String>> handlePaymentWebhook(@RequestBody PaymentWebhookRequest webhookRequest) {
        log.info("Received payment webhook: {}", webhookRequest);

        paymentService.processWebhook(webhookRequest);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Webhook processed successfully");
        response.put("orderId", webhookRequest.getOrderId());
        response.put("status", webhookRequest.getStatus());

        return ResponseEntity.ok(response);
    }
}