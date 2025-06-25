package com.cryptowallet.mapper;

import com.cryptowallet.dto.WalletDTO;
import com.cryptowallet.dto.CreateWalletRequestDTO;
import com.cryptowallet.model.WalletDocument;

public class WalletMapper {

    public static WalletDTO toDTO(WalletDocument doc) {
        return new WalletDTO(
                doc.getId(),
                doc.getUserId(),
                doc.getPublicKey(),
                doc.getBalance()
        );
    }

    public static WalletDocument fromCreateDto(CreateWalletRequestDTO dto, String publicKey, String encryptedPrivateKey) {
        WalletDocument doc = new WalletDocument();
        doc.setUserId(dto.userId());
        doc.setPublicKey(publicKey);
        doc.setEncryptedPrivateKey(encryptedPrivateKey);
        doc.setBalance(0.0);
        return doc;
    }
}