package org.scoooting.services;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.request.LoginRequestDTO;
import org.scoooting.dto.response.AuthResponseDTO;
import org.scoooting.dto.response.UserResponseDTO;
import org.scoooting.entities.User;
import org.scoooting.entities.UserRole;
import org.scoooting.exceptions.user.UserNotFoundException;
import org.scoooting.repositories.UserRepository;
import org.scoooting.repositories.UserRoleRepository;
import org.scoooting.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UserNotFoundException("Invalid credentials");
        }

        String roleName = roleRepository.findById(user.getRoleId())
                .map(UserRole::getName)
                .orElse("USER");

        String token = jwtService.generateToken(user.getEmail(), roleName);
        UserResponseDTO userDTO = userService.findUserById(user.getId());

        return new AuthResponseDTO(token, "Bearer", userDTO);
    }
}
