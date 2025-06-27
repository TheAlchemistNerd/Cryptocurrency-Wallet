package com.cryptowallet.controller;

import com.cryptowallet.dto.LoginRequestDTO;
import com.cryptowallet.dto.LoginResponseDTO;
import com.cryptowallet.dto.RegisterUserRequestDTO;
import com.cryptowallet.dto.UserDTO;
import com.cryptowallet.service.JwtService;
import com.cryptowallet.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Test
    void shouldRegisterUser() throws Exception {
        RegisterUserRequestDTO request = new RegisterUserRequestDTO("john", "pass123");
        UserDTO response = new UserDTO("1", "john");

        when(userService.registerUser(request)).thenReturn(response);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "userName": "john",
                          "password": "pass123"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("john"));
    }

    @Test
    void shouldLoginUser() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("john", "pass123");
        when(jwtService.authenticateAndGenerateToken(request)).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "userName": "john",
                          "password": "pass123"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }
}
