package com.cryptowallet.service;

import com.cryptowallet.dto.RegisterUserRequestDTO;
import com.cryptowallet.dto.UserDTO;
import com.cryptowallet.exception.UserAlreadyExistsException;
import com.cryptowallet.mapper.UserMapper;
import com.cryptowallet.model.UserDocument;
import com.cryptowallet.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO registerUser(RegisterUserRequestDTO dto) {
        log.info("Attempting to create user: {}", dto.userName());

        if (userRepository.existsByUserName((dto.userName()))) {
            log.warn("UserName already exists: {}", dto.userName());
            throw new UserAlreadyExistsException("UserName already exists");
        }

        String hashedPassword = passwordEncoder.encode(dto.password());
        UserDocument user = new UserDocument(dto.userName(), hashedPassword);
        UserDocument saved = userRepository.save(user);
        log.info("User registered with ID: {}", saved.getId());

        return UserMapper.toDTO(saved);
    }
}
