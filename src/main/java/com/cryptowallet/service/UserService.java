package com.cryptowallet.service;

import com.cryptowallet.dto.RegisterUserRequestDTO;
import com.cryptowallet.dto.UserDTO;
import com.cryptowallet.domain.Role;
import com.cryptowallet.event.UserRegisteredEvent;
import com.cryptowallet.exception.UserAlreadyExistsException;
import com.cryptowallet.exception.UserNotFoundException;
import com.cryptowallet.mapper.UserMapper;
import com.cryptowallet.model.UserDocument;
import com.cryptowallet.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing user-related operations, including registration.
 * Also implements UserDetailsService to integrate with Spring Security.
 */
@Service
@Slf4j
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username: {}", username);
        return userRepository.findByUserName(username)
                .map(userDocument -> {
                    log.debug("Found user: {}", userDocument.getUserName());
                    return new User(
                            userDocument.getUserName(),
                            userDocument.getPassword(),
                            userDocument.getRoles().stream()
                                    .map(role -> new SimpleGrantedAuthority(role.name()))
                                    .collect(Collectors.toSet())
                    );
                })
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
    }

    @Transactional
    public UserDTO registerUser(RegisterUserRequestDTO dto) {
        log.info("Attempting to create user: {}", dto.userName());

        if (userRepository.existsByUserName((dto.userName()))) {
            log.warn("UserName already exists: {}", dto.userName());
            throw new UserAlreadyExistsException("UserName already exists");
        }

        if (userRepository.existsByEmail(dto.email())) {
            log.warn("Email '{}' is already registered.", dto.email());
            throw new UserAlreadyExistsException("Email is already registered");
        }

        String hashedPassword = passwordEncoder.encode(dto.password());
        UserDocument user = new UserDocument(dto.userName(), dto.email(), hashedPassword);
        user.getRoles().add(Role.ROLE_USER);
        UserDocument saved = userRepository.save(user);
        log.info("User registered with ID: {}", saved.getId());

        UserDTO userDTO = UserMapper.toDTO(saved);
        // Publish the UserRegistered event
        eventPublisher.publishEvent(new UserRegisteredEvent(this, userDTO));
        log.info("Published UserRegisteredEvent for user ID: {}", saved.getId());

        return userDTO;
    }

    public UserDTO findUserById(String id) {
        return userRepository.findById(id)
                .map(UserMapper::toDTO)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }
}