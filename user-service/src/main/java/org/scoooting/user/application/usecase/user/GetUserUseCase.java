package org.scoooting.user.application.usecase.user;

import lombok.RequiredArgsConstructor;
import org.scoooting.user.application.dto.response.UserResponseDTO;
import org.scoooting.user.application.usecase.ToResponseDto;
import org.scoooting.user.domain.exceptions.UserNotFoundException;
import org.scoooting.user.domain.model.User;
import org.scoooting.user.domain.repositories.UserRepository;
import org.scoooting.user.application.dto.response.PageResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
public class GetUserUseCase {

    private final UserRepository userRepository;
    private final ToResponseDto toResponseDto;

    @Transactional(readOnly = true)
    public PageResponseDTO<UserResponseDTO> getUsers(String email, String name, int page, int size) {
        int offset = page * size;
        List<User> users = userRepository.findUsersWithFilters(email, name, size, offset);
        long total = userRepository.countUsersWithFilters(email, name);

        List<UserResponseDTO> userDTOs = users.stream().map(toResponseDto::execute).toList();

        int totalPages = (int) Math.ceil((double) total / size);
        return new PageResponseDTO<>(userDTOs, page, size, total, totalPages, page == 0, page >= totalPages - 1);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        return toResponseDto.execute(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return toResponseDto.execute(user);
    }

}
