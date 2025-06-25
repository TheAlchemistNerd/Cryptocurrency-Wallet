package com.cryptowallet.controller;

import com.cryptowallet.dto.CreateWalletRequestDTO;
import com.cryptowallet.dto.WalletDTO;
import com.cryptowallet.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/users/{userId}/wallets")
    public ResponseEntity<WalletDTO> createWallet(@PathVariable String userId) {
        CreateWalletRequestDTO dto = new CreateWalletRequestDTO(userId);
        WalletDTO wallet = walletService.createWallet(dto);
        return new ResponseEntity<>(wallet, HttpStatus.CREATED);
    }

    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<WalletDTO> getWallet(@PathVariable String walletId) {
        WalletDTO wallet = walletService.getWalletById(walletId);
        return ResponseEntity.ok(wallet);
    }
}
