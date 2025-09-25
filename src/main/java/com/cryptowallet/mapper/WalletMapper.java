package com.cryptowallet.mapper;

import com.cryptowallet.dto.WalletDTO;
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
}
