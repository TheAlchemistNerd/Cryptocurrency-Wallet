package com.cryptowallet.service;

import com.cryptowallet.dto.LoginRequestDTO;
import com.cryptowallet.exception.AuthenticationFailedException;
import com.cryptowallet.model.UserDocument;
import com.cryptowallet.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@Slf4j
public class JwtService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecretKey signingKey;
    private final long expirationMs = 86400000; //24hrs


    public JwtService(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${JWT_SECRET_KEY}") String jwtSecret) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String authenticateAndGenerateToken(LoginRequestDTO request) {
        UserDocument user = userRepository.findByUserName(request.userName())
                .orElseThrow(() -> {
                    log.warn("Login failed: user '{}' not found", request.userName());
                    return new AuthenticationFailedException("Invalid credentials");
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Login failed: invalid password for user '{}'", request.userName());
            throw new AuthenticationFailedException("Invalid credentials");
        }

        log.info("Generating JWT for user: {}", request.userName());

        return Jwts.builder()
                .setSubject(user.getId())
                .claim("username", user.getUserName())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
