package com.workflow.service;

import com.workflow.dto.auth.AuthResponse;
import com.workflow.dto.auth.LoginRequest;
import com.workflow.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
