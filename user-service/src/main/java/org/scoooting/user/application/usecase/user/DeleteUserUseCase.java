package org.scoooting.user.application.usecase.user;

import lombok.RequiredArgsConstructor;
import org.scoooting.user.domain.exceptions.UserNotFoundException;
import org.scoooting.user.domain.repositories.UserRepository;

@RequiredArgsConstructor
public class DeleteUserUseCase {

    private final UserRepository userRepository;

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found");
        }
        userRepository.deleteById(id);
    }
}
