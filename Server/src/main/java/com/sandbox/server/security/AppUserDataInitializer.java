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

    @Value("${app.security.seed.admin.username:admin}")
    private String adminUsername;

    @Value("${app.security.seed.admin.password:admin}")
    private String adminPassword;

    @Value("${app.security.seed.user.username:user}")
    private String userUsername;

    @Value("${app.security.seed.user.password:user}")
    private String userPassword;

    @Override
    public void run(String... args) {
        seed(adminUsername, adminPassword, AppUserRole.ROLE_ADMIN);
        seed(userUsername, userPassword, AppUserRole.ROLE_USER);
    }

    private void seed(String username, String password, AppUserRole role) {
        if (appUserRepository.findByUsername(username).isEmpty()) {
            AppUser user = new AppUser();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            appUserRepository.save(user);
        }
    }
}
