package org.scoooting.user.application.usecase;

import lombok.RequiredArgsConstructor;
import org.scoooting.user.application.dto.response.UserResponseDTO;
import org.scoooting.user.domain.exceptions.UserNotFoundException;
import org.scoooting.user.domain.model.User;
import org.scoooting.user.domain.repositories.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class AddBonusesUseCase {

    private final UserRepository userRepository;
    private final ToResponseDto toResponseDto;

    /**
     * Support/Admin can add or deduct bonuses with comment for audit
     */
    @Transactional
    public UserResponseDTO addBonuses(Long userId, Integer amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Bonus amount must be non-zero");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        int currentBonuses = user.getBonuses() != null ? user.getBonuses() : 0;
        int newBonuses = currentBonuses + amount;

        if (newBonuses < 0) {
            throw new IllegalArgumentException("Resulting bonuses cannot be negative. User has " + currentBonuses + " bonuses.");
        }

        user.setBonuses(newBonuses);
        user = userRepository.save(user);

        return toResponseDto.execute(user);
    }
}
