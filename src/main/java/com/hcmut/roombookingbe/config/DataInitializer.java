package com.hcmut.roombookingbe.config;

import com.hcmut.roombookingbe.entities.User;
import com.hcmut.roombookingbe.enums.Role;
import com.hcmut.roombookingbe.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.full-name}")
    private String adminFullName;

    @Value("${app.admin.password}")
    private String adminPassword;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        User admin = userRepository.findByEmail(adminEmail).orElseGet(() -> {
            User newAdmin = new User();
            newAdmin.setEmail(adminEmail);
            newAdmin.setFullName(adminFullName);
            newAdmin.setRole(Role.ADMIN);
            return newAdmin;
        });

        if (admin.getPassword() == null) {
            admin.setPassword(passwordEncoder.encode(adminPassword));
            userRepository.save(admin);
            log.info("Default admin password initialized: {}", adminEmail);
        }
    }
}
