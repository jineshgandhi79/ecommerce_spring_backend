package com.example.ecommerce.service;

import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.dto.PaymentResponse;
import com.example.ecommerce.dto.PaymentWebhookRequest;
import com.example.ecommerce.exception.InvalidOrderStateException;
import com.example.ecommerce.exception.ResourceNotFoundException;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.Payment;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final RestTemplate restTemplate;

    @Value("${payment.mock.service.url:http://localhost:8081}")
    private String mockPaymentServiceUrl;

    @Value("${payment.mock.service.webhook.url:http://localhost:8080/api/webhooks/payment}")
    private String webhookUrl;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository, OrderService orderService, RestTemplate restTemplate) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        log.info("Creating payment for order: {}", request.getOrderId());

        // Validate order exists
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + request.getOrderId()));

        // Validate order status
        if (!"CREATED".equals(order.getStatus())) {
            throw new InvalidOrderStateException(
                    "Order must be in CREATED state to initiate payment. Current status: " + order.getStatus()
            );
        }

        // Validate amount matches order total
        if (!order.getTotalAmount().equals(request.getAmount())) {
            throw new InvalidOrderStateException(
                    "Payment amount does not match order total. Order: " + order.getTotalAmount() + ", Payment: " + request.getAmount()
            );
        }

        // Generate payment ID
        String paymentId = "pay_mock_" + UUID.randomUUID().toString();

        // Create payment record
        Payment payment = new Payment(request.getOrderId(), request.getAmount(), "PENDING", paymentId);
        payment = paymentRepository.save(payment);

        log.info("Payment created with id: {}", payment.getId());

        // Call mock payment service (simulate external payment gateway)
        try {
            callMockPaymentService(payment);
        } catch (Exception e) {
            log.error("Failed to call mock payment service: {}", e.getMessage());
            // Payment is still created in PENDING state
        }

        // Prepare response
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setPaymentId(payment.getPaymentId());
        response.setOrderId(payment.getOrderId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setCreatedAt(payment.getCreatedAt());

        return response;
    }

    private void callMockPaymentService(Payment payment) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("paymentId", payment.getPaymentId());
            requestBody.put("orderId", payment.getOrderId());
            requestBody.put("amount", payment.getAmount());
            requestBody.put("webhookUrl", webhookUrl);

            log.info("Calling mock payment service at: {}", mockPaymentServiceUrl + "/payments/process");

            restTemplate.postForObject(
                    mockPaymentServiceUrl + "/payments/process",
                    requestBody,
                    String.class
            );

            log.info("Mock payment service called successfully");
        } catch (Exception e) {
            log.warn("Mock payment service not available or failed: {}", e.getMessage());
            // This is expected if mock service is not running
        }
    }

    @Transactional
    public void processWebhook(PaymentWebhookRequest webhookRequest) {
        log.info("Processing payment webhook for order: {}, status: {}",
                webhookRequest.getOrderId(), webhookRequest.getStatus());

        // Find payment by order ID
        Payment payment = paymentRepository.findByOrderId(webhookRequest.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment not found for order: " + webhookRequest.getOrderId()
                ));

        // Update payment status
        payment.setStatus(webhookRequest.getStatus());
        if (webhookRequest.getPaymentId() != null) {
            payment.setPaymentId(webhookRequest.getPaymentId());
        }
        paymentRepository.save(payment);

        log.info("Payment {} updated to status: {}", payment.getId(), payment.getStatus());

        // Update order status based on payment status
        String orderStatus;
        if ("SUCCESS".equals(webhookRequest.getStatus())) {
            orderStatus = "PAID";
        } else if ("FAILED".equals(webhookRequest.getStatus())) {
            orderStatus = "FAILED";
        } else {
            orderStatus = "CREATED"; // Keep in CREATED for other statuses
        }

        orderService.updateOrderStatus(webhookRequest.getOrderId(), orderStatus);
        log.info("Order {} status updated to: {}", webhookRequest.getOrderId(), orderStatus);
    }

    public Payment getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));
    }
}