package com.mastercomputeracademy.service;

import com.mastercomputeracademy.dto.request.LoginRequest;
import com.mastercomputeracademy.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
