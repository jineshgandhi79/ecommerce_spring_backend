package com.example.ecommerce.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Client to interact with Mock Payment Service
 * This is optional and mainly for demonstration
 */
@Component
public class MockPaymentServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${payment.mock.service.url:http://localhost:8081}")
    private String mockPaymentServiceUrl;

    public MockPaymentServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void processPayment(String paymentId, String orderId, Double amount, String webhookUrl) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("paymentId", paymentId);
            requestBody.put("orderId", orderId);
            requestBody.put("amount", amount);
            requestBody.put("webhookUrl", webhookUrl);

            log.info("Calling mock payment service for payment: {}", paymentId);

            restTemplate.postForObject(
                    mockPaymentServiceUrl + "/payments/process",
                    requestBody,
                    String.class
            );

            log.info("Mock payment service called successfully");
        } catch (Exception e) {
            log.warn("Failed to call mock payment service: {}", e.getMessage());
        }
    }
}