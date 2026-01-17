package org.scoooting.user.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.user.application.dto.response.AuthResult;
import org.scoooting.user.application.ports.TokenProvider;
import org.scoooting.user.domain.exceptions.InvalidRefreshTokenException;
import org.scoooting.user.domain.exceptions.UserNotFoundException;
import org.scoooting.user.domain.model.RefreshToken;
import org.scoooting.user.domain.model.User;
import org.scoooting.user.domain.model.UserRole;
import org.scoooting.user.domain.repositories.RefreshTokenRepository;
import org.scoooting.user.domain.repositories.UserRepository;
import org.scoooting.user.domain.repositories.UserRoleRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRoleRepository roleRepository;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private RefreshTokenUseCase refreshTokenUseCase;

    private User user;
    private UserRole userRole;
    private RefreshToken refreshToken;
    private AuthResult authResult;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .passwordHash("hash")
                .roleId(1L)
                .build();

        userRole = new UserRole(1L, "USER");

        refreshToken = new RefreshToken();
        refreshToken.setUserId(1L);
        refreshToken.setToken("old-refresh-token");

        authResult = new AuthResult("access-token", "new-refresh-token");
    }

    @Test
    void addRefreshToken_NewToken_CreatesToken() {
        // Arrange
        when(roleRepository.findById(1L))
                .thenReturn(Optional.of(userRole));
        when(tokenProvider.generate(1L, "Test User", "test@example.com", "USER"))
                .thenReturn(authResult);
        when(refreshTokenRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act
        AuthResult result = refreshTokenUseCase.addRefreshToken(user);

        // Assert
        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        verify(refreshTokenRepository).insert(1L, "new-refresh-token");
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void addRefreshToken_ExistingToken_UpdatesToken() {
        // Arrange
        when(roleRepository.findById(1L))
                .thenReturn(Optional.of(userRole));
        when(tokenProvider.generate(1L, "Test User", "test@example.com", "USER"))
                .thenReturn(authResult);
        when(refreshTokenRepository.findById(1L))
                .thenReturn(Optional.of(refreshToken));

        // Act
        AuthResult result = refreshTokenUseCase.addRefreshToken(user);

        // Assert
        assertNotNull(result);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).insert(anyLong(), anyString());
    }

    @Test
    void addRefreshToken_RoleNotFound_UsesDefault() {
        // Arrange
        when(roleRepository.findById(1L))
                .thenReturn(Optional.empty());
        when(tokenProvider.generate(1L, "Test User", "test@example.com", "USER"))
                .thenReturn(authResult);
        when(refreshTokenRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act
        AuthResult result = refreshTokenUseCase.addRefreshToken(user);

        // Assert
        assertNotNull(result);
        verify(tokenProvider).generate(1L, "Test User", "test@example.com", "USER");
    }

    @Test
    void refresh_ValidToken_ReturnsNewTokens() {
        // Arrange
        when(tokenProvider.getSubject("valid-token"))
                .thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(refreshTokenRepository.findById(1L))
                .thenReturn(Optional.of(refreshToken));
        when(tokenProvider.validate("old-refresh-token"))
                .thenReturn(true);
        when(roleRepository.findById(1L))
                .thenReturn(Optional.of(userRole));
        when(tokenProvider.generate(1L, "Test User", "test@example.com", "USER"))
                .thenReturn(authResult);

        // Act
        AuthResult result = refreshTokenUseCase.refresh("valid-token");

        // Assert
        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void refresh_NullToken_ThrowsException() {
        // Act & Assert
        assertThrows(InvalidRefreshTokenException.class,
                () -> refreshTokenUseCase.refresh(null));
    }

    @Test
    void refresh_UserNotFound_ThrowsException() {
        // Arrange
        when(tokenProvider.getSubject("token"))
                .thenReturn("unknown@example.com");
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> refreshTokenUseCase.refresh("token"));
    }

    @Test
    void refresh_NoRefreshToken_ThrowsException() {
        // Arrange
        when(tokenProvider.getSubject("token"))
                .thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(refreshTokenRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> refreshTokenUseCase.refresh("token"));
    }

    @Test
    void refresh_InvalidToken_DeletesAndThrows() {
        // Arrange
        when(tokenProvider.getSubject("invalid-token"))
                .thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(refreshTokenRepository.findById(1L))
                .thenReturn(Optional.of(refreshToken));
        when(tokenProvider.validate("old-refresh-token"))
                .thenReturn(false);

        // Act & Assert
        assertThrows(InvalidRefreshTokenException.class,
                () -> refreshTokenUseCase.refresh("invalid-token"));
        verify(refreshTokenRepository).delete(refreshToken);
    }
}