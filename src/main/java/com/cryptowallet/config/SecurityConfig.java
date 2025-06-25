package com.cryptowallet.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Configuration
@Slf4j
public class SecurityConfig {

    @Value("${JWT_SECRET_KEY}")
    private String jwtSecret;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.POST, "/api/users/register", "/api/users/login").permitAll()
                    .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new JwtAuthFilter(key), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    public static class JwtAuthFilter extends OncePerRequestFilter {

        private final SecretKey key;

        public JwtAuthFilter(SecretKey key) {
            this.key = key;
        }

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String path = request.getServletPath();
            return path.equals("/api/users/register") || path.equals("api/users/login");
        }

        @Override
        public void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain)
                throws ServletException, IOException {

            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer")) {
                String token = header.substring(7);

                try {
                    Claims claims = Jwts
                            .parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

                    String userId = claims.getSubject();
                    String userName = claims.get("userName", String.class);

                    var auth = new UsernamePasswordAuthenticationToken(
                            new User(userName, "", Collections.emptyList()),
                            null,
                            Collections.emptyList()
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception e) {
                    log.warn("JWT validation failed: {}", e.getMessage());
                }
            }

            chain.doFilter(request, response);
        }
    }
}