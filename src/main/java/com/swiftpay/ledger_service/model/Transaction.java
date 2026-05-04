package com.swiftpay.ledger_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private UUID id;

    private UUID senderId;
    private UUID receiverId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
}