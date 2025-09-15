package org.scoooting.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.UserDTO;
import org.scoooting.entities.User;
import org.scoooting.mappers.UserMapper;
import org.scoooting.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // Prevent deep pagination performance issues
    private static final int MAX_LIMIT = 100;
    private static final int MAX_OFFSET = 10000;

    public List<UserDTO> getUsers(int limit, int offset) {
        int validLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
        int validOffset = Math.max(0, Math.min(offset, MAX_OFFSET));

        // division by 0 can't happen due to limit validations
        int pageNumber = validOffset / validLimit;

        PageRequest pageRequest = PageRequest.of(pageNumber, validLimit);
        Page<User> users = userRepository.findAll(pageRequest);

        return users.stream().map(userMapper::toDTO).collect(Collectors.toList());
    }

    public long getTotalUserCount() {
        return userRepository.count();
    }

    public UserDTO findUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public UserDTO findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
