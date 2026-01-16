package org.scoooting.user.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.user.application.usecase.user.DeleteUserUseCase;
import org.scoooting.user.domain.exceptions.UserNotFoundException;
import org.scoooting.user.domain.repositories.UserRepository;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeleteUserUseCase deleteUserUseCase;

    @Test
    void deleteUser_Success() {
        // Arrange
        when(userRepository.existsById(1L))
                .thenReturn(true);

        // Act
        deleteUserUseCase.deleteUser(1L);

        // Assert
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.existsById(999L))
                .thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> deleteUserUseCase.deleteUser(999L));
        verify(userRepository, never()).deleteById(anyLong());
    }
}