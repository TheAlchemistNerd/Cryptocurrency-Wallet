package com.cryptowallet.controller;

import com.cryptowallet.dto.SendTransactionRequestDTO;
import com.cryptowallet.dto.TransactionDTO;
import com.cryptowallet.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService txService;

    public TransactionController(TransactionService txService) {
        this.txService = txService;
    }

    @PostMapping("/send")
    public ResponseEntity<TransactionDTO> sendTransaction(@RequestBody SendTransactionRequestDTO dto) {
        TransactionDTO response = txService.processTransaction(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
