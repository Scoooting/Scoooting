package org.scoooting.user.application.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.scoooting.user.application.dto.request.AdminUpdateUserCommand;
import org.scoooting.user.application.dto.request.UpdateUserCommand;
import org.scoooting.user.application.dto.response.UserResponseDTO;
import org.scoooting.user.application.usecase.ToResponseDto;
import org.scoooting.user.application.usecase.user.UpdateUserUseCase;
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
class UpdateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private UserRoleRepository roleRepository;

    @Mock
    private ToResponseDto toResponseDto;

    @InjectMocks
    private UpdateUserUseCase updateUserUseCase;

    private User user;
    private City city;
    private UserRole userRole;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Old Name")
                .email("old@example.com")
                .roleId(1L)
                .cityId(1L)
                .bonuses(100)
                .build();

        city = new City(2L, "MSK");
        userRole = new UserRole(2L, "ADMIN");

        userResponseDTO = new UserResponseDTO(
                1L, "New Name", "old@example.com", "USER", "MSK", 100
        );
    }

    @Test
    void updateUser_UpdateName_Success() {
        // Arrange
        UpdateUserCommand command = new UpdateUserCommand("New Name", null);
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenReturn(user);
        when(toResponseDto.execute(any(User.class)))
                .thenReturn(userResponseDTO);

        // Act
        UserResponseDTO result = updateUserUseCase.updateUser(1L, command);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(cityRepository, never()).findByName(anyString());
    }

    @Test
    void updateUser_UpdateCity_Success() {
        // Arrange
        UpdateUserCommand command = new UpdateUserCommand(null, "MSK");
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(cityRepository.findByName("MSK"))
                .thenReturn(Optional.of(city));
        when(userRepository.save(any(User.class)))
                .thenReturn(user);
        when(toResponseDto.execute(any(User.class)))
                .thenReturn(userResponseDTO);

        // Act
        UserResponseDTO result = updateUserUseCase.updateUser(1L, command);

        // Assert
        assertNotNull(result);
        verify(cityRepository).findByName("MSK");
    }

    @Test
    void updateUser_UserNotFound_ThrowsException() {
        // Arrange
        UpdateUserCommand command = new UpdateUserCommand("New Name", null);
        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> updateUserUseCase.updateUser(999L, command));
    }

    @Test
    void updateUser_CityNotFound_ThrowsException() {
        // Arrange
        UpdateUserCommand command = new UpdateUserCommand(null, "UNKNOWN");
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(cityRepository.findByName("UNKNOWN"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DataNotFoundException.class,
                () -> updateUserUseCase.updateUser(1L, command));
    }

    @Test
    void adminUpdateUser_UpdateAll_Success() {
        // Arrange
        AdminUpdateUserCommand command = new AdminUpdateUserCommand(
                "New Name", "new@example.com", "MSK", 200, "ADMIN"
        );
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com"))
                .thenReturn(Optional.empty());
        when(cityRepository.findByName("MSK"))
                .thenReturn(Optional.of(city));
        when(roleRepository.findByName("ADMIN"))
                .thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class)))
                .thenReturn(user);
        when(toResponseDto.execute(any(User.class)))
                .thenReturn(userResponseDTO);

        // Act
        UserResponseDTO result = updateUserUseCase.adminUpdateUser(1L, command);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void adminUpdateUser_EmailTaken_ThrowsException() {
        // Arrange
        User anotherUser = User.builder().id(2L).email("taken@example.com").build();
        AdminUpdateUserCommand command = new AdminUpdateUserCommand(
                null, "taken@example.com", null, null, null
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(userRepository.findByEmail("taken@example.com"))
                .thenReturn(Optional.of(anotherUser));

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class,
                () -> updateUserUseCase.adminUpdateUser(1L, command));
    }

    @Test
    void adminUpdateUser_SameEmail_Success() {
        // Arrange
        AdminUpdateUserCommand command = new AdminUpdateUserCommand(
                null, "old@example.com", null, null, null
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(userRepository.findByEmail("old@example.com"))
                .thenReturn(Optional.of(user)); // Same user
        when(userRepository.save(any(User.class)))
                .thenReturn(user);
        when(toResponseDto.execute(any(User.class)))
                .thenReturn(userResponseDTO);

        // Act
        UserResponseDTO result = updateUserUseCase.adminUpdateUser(1L, command);

        // Assert
        assertNotNull(result);
    }

    @Test
    void adminUpdateUser_RoleNotFound_ThrowsException() {
        // Arrange
        AdminUpdateUserCommand command = new AdminUpdateUserCommand(
                null, null, null, null, "UNKNOWN"
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(roleRepository.findByName("UNKNOWN"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DataNotFoundException.class,
                () -> updateUserUseCase.adminUpdateUser(1L, command));
    }
}