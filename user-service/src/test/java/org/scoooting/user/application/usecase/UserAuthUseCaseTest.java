package org.scoooting.user.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.user.application.dto.request.RegistrationCommand;
import org.scoooting.user.application.dto.request.SignInCommand;
import org.scoooting.user.application.dto.response.AuthResult;
import org.scoooting.user.application.ports.Password;
import org.scoooting.user.application.usecase.RefreshTokenUseCase;
import org.scoooting.user.application.usecase.user.UserAuthUseCase;
import org.scoooting.user.domain.exceptions.DataNotFoundException;
import org.scoooting.user.domain.exceptions.UserAlreadyExistsException;
import org.scoooting.user.domain.exceptions.UserNotFoundException;
import org.scoooting.user.domain.model.City;
import org.scoooting.user.domain.model.User;
import org.scoooting.user.domain.model.UserRole;
import org.scoooting.user.domain.repositories.CityRepository;
import org.scoooting.user.domain.repositories.UserRepository;
import org.scoooting.user.domain.repositories.UserRoleRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private Password password;

    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;

    @InjectMocks
    private UserAuthUseCase userAuthUseCase;

    private User user;
    private UserRole userRole;
    private City city;
    private AuthResult authResult;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .passwordHash("hashed-password")
                .roleId(1L)
                .build();

        userRole = new UserRole(1L, "USER");
        city = new City(1L, "SPB");
        authResult = new AuthResult("access-token", "refresh-token");
    }

    @Test
    void signIn_Success() {
        // Arrange
        SignInCommand signInCommand = new SignInCommand("test@example.com", "password123");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(password.matches("password123", "hashed-password"))
                .thenReturn(true);
        when(refreshTokenUseCase.addRefreshToken(user))
                .thenReturn(authResult);

        // Act
        AuthResult result = userAuthUseCase.signIn(signInCommand);

        // Assert
        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        verify(refreshTokenUseCase).addRefreshToken(user);
    }

    @Test
    void signIn_WrongPassword_ThrowsException() {
        // Arrange
        SignInCommand signInCommand = new SignInCommand("test@example.com", "wrong-password");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(password.matches("wrong-password", "hashed-password"))
                .thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> userAuthUseCase.signIn(signInCommand));
        verify(refreshTokenUseCase, never()).addRefreshToken(any());
    }

    @Test
    void signIn_UserNotFound_ThrowsException() {
        // Arrange
        SignInCommand signInCommand = new SignInCommand("unknown@example.com", "password");

        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> userAuthUseCase.signIn(signInCommand));
    }

    @Test
    void registerUser_Success() {
        // Arrange
        RegistrationCommand command = new RegistrationCommand(
                "new@example.com", "New User", "password123", "SPB"
        );

        when(userRepository.existsByEmail("new@example.com"))
                .thenReturn(false);
        when(userRoleRepository.findByName("USER"))
                .thenReturn(Optional.of(userRole));
        when(cityRepository.findByName("SPB"))
                .thenReturn(Optional.of(city));
        when(password.encode("password123"))
                .thenReturn("hashed-password");
        when(userRepository.save(any(User.class)))
                .thenReturn(user);
        when(refreshTokenUseCase.addRefreshToken(any(User.class)))
                .thenReturn(authResult);

        // Act
        AuthResult result = userAuthUseCase.registerUser(command);

        // Assert
        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_EmailExists_ThrowsException() {
        // Arrange
        RegistrationCommand command = new RegistrationCommand(
                "existing@example.com", "User", "password", "SPB"
        );

        when(userRepository.existsByEmail("existing@example.com"))
                .thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class,
                () -> userAuthUseCase.registerUser(command));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_RoleNotFound_ThrowsException() {
        // Arrange
        RegistrationCommand command = new RegistrationCommand(
                "new@example.com", "User", "password", "SPB"
        );

        when(userRepository.existsByEmail("new@example.com"))
                .thenReturn(false);
        when(userRoleRepository.findByName("USER"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DataNotFoundException.class,
                () -> userAuthUseCase.registerUser(command));
    }

    @Test
    void registerUser_CityNotFound_ThrowsException() {
        // Arrange
        RegistrationCommand command = new RegistrationCommand(
                "new@example.com", "User", "password", "UNKNOWN"
        );

        when(userRepository.existsByEmail("new@example.com"))
                .thenReturn(false);
        when(userRoleRepository.findByName("USER"))
                .thenReturn(Optional.of(userRole));
        when(cityRepository.findByName("UNKNOWN"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DataNotFoundException.class,
                () -> userAuthUseCase.registerUser(command));
    }
}