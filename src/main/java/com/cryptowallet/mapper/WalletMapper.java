package com.cryptowallet.mapper;

import com.cryptowallet.dto.WalletDTO;
import com.cryptowallet.model.WalletDocument;

public class WalletMapper {
    public static WalletDTO toDTO(WalletDocument doc){
        return new WalletDTO(
                doc.getId(),
                doc.getUserId(),
                doc.getPublicKey(),
                doc.getBalance()
        );
    }

    public static WalletDocument toDocument(WalletDTO dto) {
        WalletDocument doc = new WalletDocument();
        doc.setId(dto.walletId());
        doc.setUserId(dto.userId());
        doc.setPublicKey(dto.publicKey());
        doc.setBalance(dto.balance());
        return doc;
    }
}
