package com.cryptowallet.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class JwtAuthFilterTest {

    private SecretKey secretKey;
    private SecurityConfig.JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        String secret = "my-very-secure-32-byte-key-123456!";
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        jwtAuthFilter = new SecurityConfig.JwtAuthFilter(secretKey);
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetAuthenticationForValidJwt() throws Exception {
        String token = Jwts.builder()
                .setSubject("user-id-123")
                .claim("userName", "alice")
                .claim("roles", java.util.Collections.singletonList("ROLE_USER"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/transactions");
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        jwtAuthFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("alice");
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldNotFilterLoginAndRegister() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/users/register");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        jwtAuthFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateInvalidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/wallets");
        request.addHeader("Authorization", "Bearer invalid.token.here");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        jwtAuthFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }
}

