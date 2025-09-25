package com.cryptowallet.service;

import com.cryptowallet.domain.Role;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

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

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    @Test
    void registerUser_shouldSucceed_whenUserDoesNotExist() {
        // Arrange
        RegisterUserRequestDTO dto = new RegisterUserRequestDTO("testuser", "test@test.com", "password");
        when(userRepository.existsByUserName("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(userRepository.save(any(UserDocument.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserDTO result = userService.registerUser(dto);

        // Assert
        assertThat(result.userName()).isEqualTo("testuser");
        assertThat(result.email()).isEqualTo("test@test.com");
        assertThat(result.roles()).contains("ROLE_USER");
    }

    @Test
    void registerUser_shouldThrowUserAlreadyExistsException_whenUsernameExists() {
        // Arrange
        RegisterUserRequestDTO dto = new RegisterUserRequestDTO("testuser", "test@test.com", "password");
        when(userRepository.existsByUserName("testuser")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.registerUser(dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("UserName already exists");
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // Arrange
        UserDocument userDoc = new UserDocument("testuser", "test@test.com", "password");
        userDoc.setRoles(Set.of(Role.ROLE_USER));
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(userDoc));

        // Act
        UserDetails userDetails = userService.loadUserByUsername("testuser");

        // Assert
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("password");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.loadUserByUsername("testuser"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with username: testuser");
    }
}
