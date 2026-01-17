package org.scoooting.user.application.usecase;

import lombok.RequiredArgsConstructor;
import org.scoooting.user.application.dto.response.AuthResult;
import org.scoooting.user.application.ports.TokenProvider;
import org.scoooting.user.domain.exceptions.UserNotFoundException;
import org.scoooting.user.domain.model.RefreshToken;
import org.scoooting.user.domain.model.User;
import org.scoooting.user.domain.model.UserRole;
import org.scoooting.user.domain.repositories.RefreshTokenRepository;
import org.scoooting.user.domain.repositories.UserRepository;
import org.scoooting.user.domain.repositories.UserRoleRepository;
import org.scoooting.user.domain.exceptions.InvalidRefreshTokenException;

import java.util.Optional;

@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRoleRepository roleRepository;
    private final TokenProvider tokenProvider;

    public AuthResult addRefreshToken(User user) {
        String roleName = roleRepository.findById(user.getRoleId())
                .map(UserRole::getName)
                .orElse("USER");
        AuthResult authResult = tokenProvider.generate(user.getId(), user.getName(),
                user.getEmail(), roleName);

        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findById(user.getId());
        if (optionalRefreshToken.isEmpty()) {
            refreshTokenRepository.insert(user.getId(), authResult.refreshToken());
        } else {
            RefreshToken refreshToken = optionalRefreshToken.get();
            refreshToken.setToken(authResult.refreshToken());
            refreshTokenRepository.save(refreshToken);
        }

        return authResult;
    }

    public AuthResult refresh(String token) {
        if (token == null)
            throw new InvalidRefreshTokenException("Invalid refresh token!");

        String email = tokenProvider.getSubject(token);
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findById(user.getId());
            if (optionalRefreshToken.isPresent()) {
                RefreshToken refreshToken = optionalRefreshToken.get();
                if (tokenProvider.validate(refreshToken.getToken())) {
                    return addRefreshToken(user);
                }

                refreshTokenRepository.delete(refreshToken);
                throw new InvalidRefreshTokenException("Invalid refresh token!");
            }
        }

        throw new UserNotFoundException("User not found!");
    }

}
