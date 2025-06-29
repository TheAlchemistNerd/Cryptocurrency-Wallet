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
import org.springframework.security.core.GrantedAuthority; // Import for GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Import for SimpleGrantedAuthority

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List; // Import for List
import java.util.ArrayList; // Import for ArrayList

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
                        .anyRequest().authenticated() // All other requests must be authenticated
                )
                .csrf(csrf -> csrf.disable()) // Disable CSRF protection
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless sessions
                .httpBasic(httpBasic -> httpBasic.disable()) // Explicitly disable HTTP Basic
                .addFilterBefore(new JwtAuthFilter(key), UsernamePasswordAuthenticationFilter.class); // Add custom JWT filter

        return http.build();
    }

    // Custom filter to process JWT tokens on each request
    public static class JwtAuthFilter extends OncePerRequestFilter {

        private final SecretKey key;

        public JwtAuthFilter(SecretKey key) {
            this.key = key;
        }

        // Defines paths that should not be processed by this filter
        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            String path = request.getServletPath();
            return path.equals("/api/users/register") || path.equals("/api/users/login");
        }

        // The core logic of the filter
        @Override
        public void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain chain)
                throws ServletException, IOException {

            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) { // Note the space after Bearer
                String token = header.substring(7);

                try {
                    Claims claims = Jwts
                            .parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

                    String userId = claims.getSubject();
                    String userName = claims.get("username", String.class); // Use 'username' as per your JwtServiceTest

                    // --- MODIFICATION HERE: Add roles/authorities ---
                    // You can fetch roles from claims if stored, or assign a default role
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // Assign a default role

                    // Create the authentication token with authorities
                    var auth = new UsernamePasswordAuthenticationToken(
                            new User(userName, "", authorities), // Principal with username and authorities
                            null, // Credentials (not needed after token validation)
                            authorities // Authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);

                } catch (Exception e) {
                    log.warn("JWT validation failed: {}", e.getMessage());
                    SecurityContextHolder.clearContext();
                }
            }

            chain.doFilter(request, response);
        }
    }
}
