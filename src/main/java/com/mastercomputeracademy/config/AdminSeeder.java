package com.mastercomputeracademy.config;

import com.mastercomputeracademy.entity.AdminUser;
import com.mastercomputeracademy.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the first admin account on application startup if none exists.
 *
 * IMPORTANT NOTES:
 * ─────────────────────────────────────────────────────────────────────
 * 1. This runner is active only on the "dev" profile.
 *    It is DISABLED on the "prod" profile (use @Profile("!prod") below if you
 *    want to allow it in staging; otherwise restrict strictly to dev).
 *
 * 2. Credentials come exclusively from environment variables:
 *      ADMIN_EMAIL    – admin email address
 *      ADMIN_PASSWORD – admin password (stored as BCrypt hash, NEVER plain-text)
 *
 * 3. Change the default credentials immediately after the first login in any
 *    environment other than a personal local machine.
 *
 * 4. For production, create the admin account via a secure one-time migration
 *    or a protected internal endpoint, then REMOVE or disable this seeder.
 * ─────────────────────────────────────────────────────────────────────
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements ApplicationRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.email}")
    private String adminEmail;

    @Value("${app.seed.admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (adminUserRepository.existsByEmail(adminEmail)) {
            log.info("Seed admin already exists – skipping seeding (email: {})", adminEmail);
            return;
        }

        AdminUser admin = AdminUser.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(AdminUser.Role.ADMIN)
                .build();

        adminUserRepository.save(admin);

        log.warn("════════════════════════════════════════════════════════════");
        log.warn("  SEED ADMIN CREATED");
        log.warn("  Email   : {}", adminEmail);
        log.warn("  Password: [REDACTED – see ADMIN_PASSWORD env var]");
        log.warn("  ACTION REQUIRED: Change this password immediately!");
        log.warn("════════════════════════════════════════════════════════════");
    }
}
