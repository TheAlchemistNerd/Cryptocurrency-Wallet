package com.cryptowallet.mapper;

import com.cryptowallet.dto.WalletDTO;
import com.cryptowallet.dto.CreateWalletRequestDTO;
import com.cryptowallet.model.WalletDocument;

public class WalletMapper {

    public static WalletDTO toDTO(WalletDocument doc) {
        return new WalletDTO(
                doc.getId(),
                doc.getUserId(),
                doc.getAddress(),
                doc.getBalances()
        );
    }

    public static WalletDocument fromCreateDto(CreateWalletRequestDTO dto, String address, String encryptedPrivateKey) {
        return new WalletDocument(dto.userId(), address, encryptedPrivateKey);
    }
}