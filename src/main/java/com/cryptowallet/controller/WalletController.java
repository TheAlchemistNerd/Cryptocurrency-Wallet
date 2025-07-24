package com.cryptowallet.controller;

import com.cryptowallet.dto.CreateWalletRequestDTO;
import com.cryptowallet.dto.WalletDTO;
import com.cryptowallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Wallet", description = "Wallet management APIs")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/users/{userId}/wallets")
    @Operation(summary = "Create a new wallet for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Wallet created successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<WalletDTO> createWallet(@PathVariable String userId) {
        CreateWalletRequestDTO dto = new CreateWalletRequestDTO(userId);
        WalletDTO wallet = walletService.createWallet(dto);
        return new ResponseEntity<>(wallet, HttpStatus.CREATED);
    }

    @GetMapping("/wallets/{walletId}")
    @Operation(summary = "Get wallet by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet found"),
            @ApiResponse(responseCode = "404", description = "Wallet not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<WalletDTO> getWallet(@PathVariable String walletId) {
        WalletDTO wallet = walletService.getWalletById(walletId);
        return ResponseEntity.ok(wallet);
    }
}
