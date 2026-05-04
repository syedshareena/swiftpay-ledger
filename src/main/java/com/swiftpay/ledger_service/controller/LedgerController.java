package com.swiftpay.ledger_service.controller;

import com.swiftpay.ledger_service.model.Transaction;
import com.swiftpay.ledger_service.service.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/ledger")
@RequiredArgsConstructor
@Tag(name = "Ledger Service", description = "APIs for transaction history")
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/transactions/{userId}")
    @Operation(summary = "Get transaction history for a user")
    public ResponseEntity<List<Transaction>> getTransactionHistory(
            @PathVariable UUID userId) {
        List<Transaction> transactions = ledgerService.getTransactionHistory(userId);
        return ResponseEntity.ok(transactions);
    }
}