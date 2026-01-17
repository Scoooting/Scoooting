package org.scoooting.user.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.user.application.dto.response.UserResponseDTO;
import org.scoooting.user.domain.exceptions.UserNotFoundException;
import org.scoooting.user.domain.model.User;
import org.scoooting.user.domain.repositories.UserRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AddBonusesUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ToResponseDto toResponseDto;

    @InjectMocks
    private AddBonusesUseCase addBonusesUseCase;

    private User user;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .bonuses(100)
                .roleId(1L)
                .build();

        userResponseDTO = new UserResponseDTO(1L, "Test User", "test@example.com",
                "USER", null, 150);
    }

    @Test
    void addBonuses_Success() {
        // Arrange
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenReturn(user);
        when(toResponseDto.execute(any(User.class)))
                .thenReturn(userResponseDTO);

        // Act
        UserResponseDTO result = addBonusesUseCase.addBonuses(1L, 50);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addBonuses_NullAmount_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> addBonusesUseCase.addBonuses(1L, null));

        verify(userRepository, never()).save(any());
    }

    @Test
    void addBonuses_NegativeResult_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> addBonusesUseCase.addBonuses(1L, -150));

        verify(userRepository, never()).save(any());
    }

    @Test
    void addBonuses_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> addBonusesUseCase.addBonuses(999L, 50));
    }

    @Test
    void addBonuses_NullInitialBonuses_StartsFromZero() {
        // Arrange
        user.setBonuses(null);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenReturn(user);
        when(toResponseDto.execute(any(User.class)))
                .thenReturn(userResponseDTO);

        // Act
        UserResponseDTO result = addBonusesUseCase.addBonuses(1L, 50);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(argThat(u -> u.getBonuses() == 50));
    }
}