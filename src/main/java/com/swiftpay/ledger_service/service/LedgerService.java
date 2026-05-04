package com.swiftpay.ledger_service.service;

import com.swiftpay.ledger_service.dto.PaymentEvent;
import com.swiftpay.ledger_service.model.Account;
import com.swiftpay.ledger_service.model.Transaction;
import com.swiftpay.ledger_service.repository.AccountRepository;
import com.swiftpay.ledger_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public void processPayment(PaymentEvent event) {
        Account sender = accountRepository.findByUserId(event.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        Account receiver = accountRepository.findByUserId(event.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (sender.getBalance().compareTo(event.getAmount()) < 0) {
            updateTransactionStatus(event.getTransactionId(), "FAILED");
            kafkaTemplate.send("payment-failed", event.getTransactionId().toString());
            log.warn("Payment failed - insufficient funds: {}", event.getTransactionId());
            return;
        }

        // Atomic debit and credit
        sender.setBalance(sender.getBalance().subtract(event.getAmount()));
        receiver.setBalance(receiver.getBalance().add(event.getAmount()));
        accountRepository.saveAll(List.of(sender, receiver));

        updateTransactionStatus(event.getTransactionId(), "COMPLETED");
        kafkaTemplate.send("payment-completed", event.getTransactionId().toString());
        log.info("Payment completed: {}", event.getTransactionId());
    }

    private void updateTransactionStatus(UUID transactionId, String status) {
        transactionRepository.findById(transactionId).ifPresent(txn -> {
            txn.setStatus(status);
            transactionRepository.save(txn);
        });
    }

    public List<Transaction> getTransactionHistory(UUID userId) {
        return transactionRepository.findBySenderId(userId);
    }
}