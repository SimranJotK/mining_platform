package com.cryptomining.platform.config;

import com.cryptomining.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class PasswordInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        var seedUsers = java.util.Map.of(
            "creator@platform.local", "Creator@123",
            "admin@platform.local", "Admin@123",
            "user@platform.local", "User@123"
        );

        seedUsers.forEach((email, password) ->
            userRepository.findByEmail(email).ifPresent(user -> {
                if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                    user.setPasswordHash(passwordEncoder.encode(password));
                    userRepository.save(user);
                    log.info("Updated password for seed user: {}", email);
                }
            })
        );
    }
}
