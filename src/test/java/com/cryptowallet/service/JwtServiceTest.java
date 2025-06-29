package com.cryptowallet.service;

import com.cryptowallet.dto.LoginRequestDTO;
import com.cryptowallet.exception.AuthenticationFailedException;
import com.cryptowallet.model.UserDocument;
import com.cryptowallet.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // Use Mockito extension for JUnit 5
public class JwtServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtService jwtService;

    // Test constants
    private final String TEST_SECRET_KEY = "a-secure-key-that-is-long-enough-for-hs256";
    private final long TEST_EXPIRATION_MS = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        // Manually instantiate the service with mocks and test values for @Value fields
        jwtService = new JwtService(userRepository, passwordEncoder, TEST_SECRET_KEY, TEST_EXPIRATION_MS);
    }

    @Test
    void shouldGenerateTokenIfCredentialsValid() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "password123");
        UserDocument userDoc = new UserDocument("testuser", "test@example.com", "hashed_password");
        userDoc.setId("user-id-001");

        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(userDoc));
        when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);

        // Act
        String token = jwtService.authenticateAndGenerateToken(loginRequest);

        // Assert
        assertThat(token).isNotBlank();

        // Verify the contents of the generated token
        SecretKey signingKey = Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo("user-id-001");
        assertThat(claims.get("username", String.class)).isEqualTo("testuser");
        assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");

        // Verify expiration is set correctly
        Date expiration = claims.getExpiration();
        Date issuedAt = claims.getIssuedAt();
        assertThat(expiration.getTime() - issuedAt.getTime()).isEqualTo(TEST_EXPIRATION_MS);
    }

    @Test
    void shouldThrowIfUserNotFound() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("nonexistent", "password123");
        when(userRepository.findByUserName("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jwtService.authenticateAndGenerateToken(loginRequest))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void shouldThrowException_whenPasswordIsInvalid() {
        // Arrange
        LoginRequestDTO loginRequest = new LoginRequestDTO("testuser", "wrong_password");
        UserDocument userDoc = new UserDocument("testuser", "test@example.com", "hashed_password");
        userDoc.setId("user-id-001");

        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(userDoc));
        when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> jwtService.authenticateAndGenerateToken(loginRequest))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid username or password");
    }
}
