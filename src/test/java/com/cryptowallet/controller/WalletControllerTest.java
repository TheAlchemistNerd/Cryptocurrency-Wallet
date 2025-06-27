package com.cryptowallet.controller;

import com.cryptowallet.dto.WalletDTO;
import com.cryptowallet.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @Test
    void shouldCreateWallet() throws Exception {
        WalletDTO dto = new WalletDTO("w1", "u1", "publicKey123", 0.0);
        when(walletService.createWallet(new com.cryptowallet.dto.CreateWalletRequestDTO("u1"))).thenReturn(dto);

        mockMvc.perform(post("/api/users/u1/wallets"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.walletId").value("w1"));
    }

    @Test
    void shouldGetWallet() throws Exception {
        WalletDTO dto = new WalletDTO("w1", "u1", "publicKey123", 10.5);
        when(walletService.getWalletById("w1")).thenReturn(dto);

        mockMvc.perform(get("/api/wallets/w1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value("w1"))
                .andExpect(jsonPath("$.balance").value(10.5));
    }
}
