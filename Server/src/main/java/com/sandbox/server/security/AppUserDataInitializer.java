package com.sandbox.server.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class AppUserDataInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.seed.username:admin}")
    private String seedUsername;

    @Value("${app.security.seed.password:admin}")
    private String seedPassword;

    @Override
    public void run(String... args) {
        if (appUserRepository.findByUsername(seedUsername).isEmpty()) {
            AppUser user = new AppUser();
            user.setUsername(seedUsername);
            user.setPassword(passwordEncoder.encode(seedPassword));
            user.setRole(AppUserRole.ROLE_ADMIN);
            appUserRepository.save(user);
        }
    }
}
