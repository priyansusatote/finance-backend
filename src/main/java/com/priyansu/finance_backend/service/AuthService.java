package com.priyansu.finance_backend.service;

import com.priyansu.finance_backend.dto.LoginRequest;

public interface AuthService {
    String login(LoginRequest request);
}