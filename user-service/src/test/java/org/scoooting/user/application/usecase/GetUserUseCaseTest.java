package org.scoooting.user.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.user.application.dto.response.PageResponseDTO;
import org.scoooting.user.application.dto.response.UserResponseDTO;
import org.scoooting.user.application.usecase.ToResponseDto;
import org.scoooting.user.application.usecase.user.GetUserUseCase;
import org.scoooting.user.domain.exceptions.UserNotFoundException;
import org.scoooting.user.domain.model.User;
import org.scoooting.user.domain.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ToResponseDto toResponseDto;

    @InjectMocks
    private GetUserUseCase getUserUseCase;

    private User user;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("Test User")
                .roleId(1L)
                .build();

        userResponseDTO = new UserResponseDTO(
                1L, "test@example.com", "Test User", "USER", "SPB", 0
        );
    }

    @Test
    void getUsers_Success() {
        // Arrange
        when(userRepository.findUsersWithFilters("test@", "Test", 20, 0))
                .thenReturn(List.of(user));
        when(userRepository.countUsersWithFilters("test@", "Test"))
                .thenReturn(1L);
        when(toResponseDto.execute(any(User.class)))
                .thenReturn(userResponseDTO);

        // Act
        PageResponseDTO<UserResponseDTO> result = getUserUseCase.getUsers(
                "test@", "Test", 0, 20
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
        assertTrue(result.first());
        assertTrue(result.last());
    }

    @Test
    void getUsers_EmptyResult() {
        // Arrange
        when(userRepository.findUsersWithFilters(null, null, 20, 0))
                .thenReturn(List.of());
        when(userRepository.countUsersWithFilters(null, null))
                .thenReturn(0L);

        // Act
        PageResponseDTO<UserResponseDTO> result = getUserUseCase.getUsers(
                null, null, 0, 20
        );

        // Assert
        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
    }

    @Test
    void findUserById_Success() {
        // Arrange
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(toResponseDto.execute(user))
                .thenReturn(userResponseDTO);

        // Act
        UserResponseDTO result = getUserUseCase.findUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Test User", result.name());
    }

    @Test
    void findUserById_NotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> getUserUseCase.findUserById(999L));
    }

    @Test
    void findUserByEmail_Success() {
        // Arrange
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(toResponseDto.execute(user))
                .thenReturn(userResponseDTO);

        // Act
        UserResponseDTO result = getUserUseCase.findUserByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.email());
    }

    @Test
    void findUserByEmail_NotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> getUserUseCase.findUserByEmail("unknown@example.com"));
    }
}