package com.cryptowallet.service;

import com.cryptowallet.crypto.CryptoFacade;
import com.cryptowallet.dto.SendTransactionRequestDTO;
import com.cryptowallet.dto.TransactionDTO;
import com.cryptowallet.model.TransactionDocument;
import com.cryptowallet.model.WalletDocument;
import com.cryptowallet.repository.TransactionRepository;
import com.cryptowallet.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TransactionService {

    private final WalletRepository walletRepo;
    private final TransactionRepository txRepo;
    private final CryptoFacade cryptoFacade;

    public TransactionService(WalletRepository walletRepo, TransactionRepository txRepo, CryptoFacade cryptoFacade) {
        this.walletRepo = walletRepo;
        this.txRepo = txRepo;
        this.cryptoFacade = cryptoFacade;
    }

    public TransactionDTO processTransaction(SendTransactionRequestDTO dto) {
        WalletDocument sender = walletRepo.findById(dto.senderWalletId()).orElseThrow();
        WalletDocument receiver = walletRepo.findByUserId(dto.receiverAddress()).stream().findFirst().orElseThrow();

        String signature = cryptoFacade.signData(
            dto.senderWalletId() + dto.receiverAddress() + dto.amount(),
            sender.getEncryptedPrivateKey()
        );

        sender.setBalance(sender.getBalance() - dto.amount());
        receiver.setBalance(receiver.getBalance() + dto.amount());
        walletRepo.save(sender);
        walletRepo.save(receiver);

        TransactionDocument tx = new TransactionDocument(
            sender.getPublicKey(),
            receiver.getPublicKey(),
            dto.amount(),
            new Date(),
            signature
        );

        TransactionDocument saved = txRepo.save(tx);

        return new TransactionDTO(
                saved.getId(),
                saved.getSenderAddress(),
                saved.getReceiverAddress(),
                saved.getAmount(),
                saved.getTimestamp(),
                saved.getSignature()
        );
    }
}
