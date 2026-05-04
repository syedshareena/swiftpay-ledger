package com.swiftpay.ledger_service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftpay.ledger_service.dto.PaymentEvent;
import com.swiftpay.ledger_service.service.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {

    private final LedgerService ledgerService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "payment-initiated", groupId = "ledger-group")
    public void consumePaymentInitiated(String message) {
        try {
            PaymentEvent event = objectMapper.readValue(message, PaymentEvent.class);
            log.info("Received payment event: {}", event.getTransactionId());
            ledgerService.processPayment(event);
        } catch (Exception e) {
            log.error("Error processing payment event: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}