package com.cryptowallet.service;

import com.cryptowallet.dto.RegisterUserRequestDTO;
import com.cryptowallet.dto.UserDTO;
import com.cryptowallet.exception.UserAlreadyExistsException;
import com.cryptowallet.model.UserDocument;
import com.cryptowallet.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_shouldSucceed() {
        RegisterUserRequestDTO dto = new RegisterUserRequestDTO("john.doe", "john.doe@example.com", "password123");
        UserDocument userDoc = new UserDocument("john.doe", "john.doe@example.com", "hashed_password");
        userDoc.setId("user-id-1");

        when(userRepository.existsByUserName("john.doe")).thenReturn(false);
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(userRepository.save(any(UserDocument.class))).thenReturn(userDoc);

        UserDTO result = userService.registerUser(dto);

        assertThat(result.userName()).isEqualTo("john.doe");
        assertThat(result.email()).isEqualTo("john.doe@example.com");
        assertThat(result.id()).isEqualTo("user-id-1");
    }

    @Test
    void registerUser_shouldFailIfUsernameExists() {
        RegisterUserRequestDTO dto = new RegisterUserRequestDTO("john.doe", "john.doe@example.com", "password123");
        when(userRepository.existsByUserName("john.doe")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("UserName already exists");
    }
}