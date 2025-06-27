package com.cryptowallet.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SecurityConfigTest {

    private SecurityConfig.JwtAuthFilter jwtAuthFilter;
    private SecretKey secretKey;

    @BeforeEach
    public void setUp() {
        String secret = "my32ByteSecretKeyMustBeSecure2!&)($&$^##*@*482099!";
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtAuthFilter = new SecurityConfig.JwtAuthFilter(secretKey);
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldNotFilterRegisterOrLoginPath() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/users/register");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        jwtAuthFilter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    public void shouldAuthenticateValidJwtToken() throws ServletException, IOException {
        String username = "testuser";
        String userId = "user-123";

        String token = Jwts.builder()
                .setSubject(userId)
                .claim("userName", username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/wallets");
        request.addHeader("Authorization", "Bearer " + token);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        jwtAuthFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isInstanceOf(org.springframework.security.core.userdetails.User.class);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities()).isEqualTo(Collections.emptyList());

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    public void shouldNotAuthenticateInvalidJwtToken() throws ServletException, IOException {
        String invalidToken = "Bearer malformed.token.here";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/wallets");
        request.addHeader("Authorization", invalidToken);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        jwtAuthFilter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain, times(1)).doFilter(request, response);
    }
}
