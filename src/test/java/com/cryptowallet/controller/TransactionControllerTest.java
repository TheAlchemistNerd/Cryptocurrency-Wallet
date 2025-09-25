package com.cryptowallet.controller;

import com.cryptowallet.dto.SendTransactionRequestDTO;
import com.cryptowallet.dto.TransactionDTO;
import com.cryptowallet.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 1. Use @WebMvcTest to focus only on the TransactionController.
// This is much faster and avoids loading the full application context.
@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    // @WebMvcTest automatically configures MockMvc.
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    // ObjectMapper can be autowired to help create JSON strings
    @Autowired
    private ObjectMapper objectMapper;

    // 2. Add @WithMockUser to simulate an authenticated request.
    // This satisfies Spring Security's requirement that the user be logged in.
    @Test
    @WithMockUser
    void shouldSendTransaction() throws Exception {
        // Arrange
        SendTransactionRequestDTO requestDto = new SendTransactionRequestDTO(
                "addr1", "addr2", new BigDecimal("10.5"), "a-valid-signature", "BTC");

        TransactionDTO responseDto = new TransactionDTO(
                "tx-id-1", "addr1", "addr2", new BigDecimal("10.5"), "BTC", "a-valid-signature", Instant.now(), com.cryptowallet.domain.TransactionStatus.PENDING);

        when(transactionService.processTransaction(any(SendTransactionRequestDTO.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/transactions/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        // Use ObjectMapper to safely convert the request object to a JSON string
                        .content(objectMapper.writeValueAsString(requestDto))
                        // 3. Add .with(csrf()) to include a valid CSRF token in the request,
                        // which is good practice for tests even if disabled in the main app.
                        .with(csrf()))
                .andExpect(status().isCreated()) // Now expects 201 Created
                .andExpect(jsonPath("$.id").value("tx-id-1"))
                .andExpect(jsonPath("$.amount").value(10.5));
    }
}
