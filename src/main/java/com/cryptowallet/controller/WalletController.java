package com.cryptowallet.controller;

import com.cryptowallet.dto.CreateWalletRequestDTO;
import com.cryptowallet.dto.WalletDTO;
import com.cryptowallet.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    public ResponseEntity<WalletDTO> createWallet(@RequestBody CreateWalletRequestDTO dto) {
        WalletDTO wallet = walletService.createWallet(dto);
        return new ResponseEntity<>(wallet, HttpStatus.CREATED);
    }
}
