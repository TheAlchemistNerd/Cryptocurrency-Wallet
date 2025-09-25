package com.cryptowallet.controller;

import com.cryptowallet.dto.LoginRequestDTO;
import com.cryptowallet.dto.RegisterUserRequestDTO;
import com.cryptowallet.dto.UserDTO;
import com.cryptowallet.service.JwtService;
import com.cryptowallet.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// Import for Spring Security Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Test
    void shouldRegisterUser() throws Exception {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO("john.doe", "john.doe@example.com", "password123");
        UserDTO response = new UserDTO("user-id-123", "john.doe", "john.doe@example.com", java.util.Set.of("ROLE_USER"));

        when(userService.registerUser(any(RegisterUserRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "userName": "john.doe",
                          "email": "john.doe@example.com",
                          "password": "password123"
                        }
                        """)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value("john.doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void shouldLoginUser() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("john.doe", "password123");
        when(jwtService.authenticateAndGenerateToken(any(LoginRequestDTO.class))).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "userName": "john.doe",
                          "password": "password123"
                        }
                        """)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }
}
