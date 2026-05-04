package com.swiftpay.ledger_service.repository;

import com.swiftpay.ledger_service.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findBySenderId(UUID senderId);
    List<Transaction> findByReceiverId(UUID receiverId);
}