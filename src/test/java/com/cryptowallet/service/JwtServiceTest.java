package com.cryptowallet.service;

import com.cryptowallet.dto.LoginRequestDTO;
import com.cryptowallet.exception.AuthenticationFailedException;
import com.cryptowallet.model.UserDocument;
import com.cryptowallet.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JwtServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = new JwtService(userRepository, passwordEncoder, "mySecretKey123456789012345678901234");

    }

    @Test
    void shouldGenerateTokenIfCredentialsValid() {
        LoginRequestDTO request = new LoginRequestDTO("alice", "secret");
        UserDocument user = new UserDocument("alice", "hashed");
        user.setId("uid-1");

        when(userRepository.findByUserName("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);

        String token = jwtService.authenticateAndGenerateToken(request);

        assertThat(token).isNotBlank();

        Claims claims = Jwts.parserBuilder()
                .setSigningKey("mySecretKey123456789012345678901234".getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo("uid-1");
    }

    @Test
    void shouldThrowIfUserNotFound() {
        LoginRequestDTO request = new LoginRequestDTO("bad", "pwd");

        when(userRepository.findByUserName("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jwtService.authenticateAndGenerateToken(request))
                .isInstanceOf(AuthenticationFailedException.class);
    }
}
