package com.mastercomputeracademy.service.impl;

import com.mastercomputeracademy.dto.request.LoginRequest;
import com.mastercomputeracademy.dto.response.LoginResponse;
import com.mastercomputeracademy.entity.AdminUser;
import com.mastercomputeracademy.exception.BadCredentialsException;
import com.mastercomputeracademy.repository.AdminUserRepository;
import com.mastercomputeracademy.security.JwtUtil;
import com.mastercomputeracademy.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.debug("Login attempt for email: {}", request.getEmail());

        AdminUser admin = adminUserRepository.findByEmail(request.getEmail())
                .orElseThrow(BadCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            // Use a generic message to avoid revealing which field was wrong
            throw new BadCredentialsException();
        }

        String token = jwtUtil.generateToken(admin.getEmail(), admin.getRole().name());
        log.info("Successful login for admin: {}", admin.getEmail());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .build();
    }
}
