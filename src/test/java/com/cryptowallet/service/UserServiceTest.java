package com.cryptowallet.service;

import com.cryptowallet.dto.RegisterUserRequestDTO;
import com.cryptowallet.dto.UserDTO;
import com.cryptowallet.exception.UserAlreadyExistsException;
import com.cryptowallet.model.UserDocument;
import com.cryptowallet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void testRegisterUserSuccess() {
        RegisterUserRequestDTO dto = new RegisterUserRequestDTO("john", "1234");

        when(userRepository.existsByUserName("john")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("hashed123");
        when(userRepository.save(any(UserDocument.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        UserDTO result = userService.registerUser(dto);

        assertEquals("john", result.userName());
    }

    @Test
    void testRegisterUserFailsIfExists() {
        RegisterUserRequestDTO dto = new RegisterUserRequestDTO("john", "1234");
        when(userRepository.existsByUserName("john")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () ->
                userService.registerUser(dto));
    }
}

