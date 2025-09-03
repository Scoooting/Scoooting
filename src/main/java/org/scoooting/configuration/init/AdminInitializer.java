package org.scoooting.configuration.init;

import lombok.RequiredArgsConstructor;
import org.scoooting.entities.User;
import org.scoooting.entities.enums.Roles;
import org.scoooting.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        Optional<User> optionalUser = userRepository.findByName(adminUsername);
        if (optionalUser.isEmpty()) {
            userRepository.save(User.builder()
                    .name(adminUsername)
                    .email(adminEmail)
                    .password(adminPassword)
                    .role(Roles.ADMIN)
                    .build());
        }
    }
}
