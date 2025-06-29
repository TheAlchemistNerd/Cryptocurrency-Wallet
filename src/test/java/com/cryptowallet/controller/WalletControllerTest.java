package com.cryptowallet.controller;

import com.cryptowallet.dto.CreateWalletRequestDTO;
import com.cryptowallet.dto.WalletDTO;
import com.cryptowallet.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import java.math.BigDecimal;
import java.util.Map;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
public class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    // ObjectMapper can be autowired to help create JSON strings
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"}) // Simulate an authenticated user
    void shouldCreateWallet() throws Exception {
        // Arrange
        String userId = "user-id-1";
        CreateWalletRequestDTO requestDto = new CreateWalletRequestDTO(userId); // Create DTO for clarity
        WalletDTO responseDto = new WalletDTO("wallet-id-1", userId, "publicKey123", Map.of("USD", BigDecimal.ZERO));

        // Mock the service call
        when(walletService.createWallet(any(CreateWalletRequestDTO.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/users/{userId}/wallets", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    // Use ObjectMapper to safely convert the request object to a JSON string
                    .content(objectMapper.writeValueAsString(requestDto))
                    // 3. Add .with(csrf()) to include a valid CSRF token in the request,
                    // which is good practice for tests even if disabled in the main app.
                    .with(csrf()))// Send an empty JSON body if the controller expects @RequestBody, even if it's just userId from path
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("wallet-id-1"))
                .andExpect(jsonPath("$.address").value("publicKey123"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"}) // Simulate an authenticated user
    void shouldGetWallet() throws Exception {
        // Arrange
        String walletId = "wallet-id-1";
        WalletDTO responseDto = new WalletDTO(walletId, "user-id-1", "publicKey123", Map.of("BTC", new BigDecimal("1.5")));

        // Mock the service call
        when(walletService.getWalletById(walletId)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/wallets/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(walletId))
                .andExpect(jsonPath("$.balances.BTC").value(1.5));
    }
}
