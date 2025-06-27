package com.cryptowallet.controller;

import com.cryptowallet.dto.SendTransactionRequestDTO;
import com.cryptowallet.dto.TransactionDTO;
import com.cryptowallet.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    void shouldSendTransaction() throws Exception {
        SendTransactionRequestDTO request = new SendTransactionRequestDTO("wallet1", "publicKey2", 10.0);
        TransactionDTO response = new TransactionDTO("tx1", "publicKey1", "publicKey2", 10.0, new Date(), "sig");

        when(transactionService.processTransaction(request)).thenReturn(response);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "senderWalletId": "wallet1",
                          "receiverAddress": "publicKey2",
                          "amount": 10.0
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("tx1"));
    }
}