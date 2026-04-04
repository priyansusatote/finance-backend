package com.priyansu.finance_backend.service.Impl;

import com.priyansu.finance_backend.dto.LoginRequest;
import com.priyansu.finance_backend.entity.User;
import com.priyansu.finance_backend.exception.BadRequestException;
import com.priyansu.finance_backend.exception.ResourceNotFoundException;
import com.priyansu.finance_backend.repository.UserRepository;
import com.priyansu.finance_backend.security.JwtService;
import com.priyansu.finance_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // TEMP: plain password (later BCrypt)
        if (!user.getPassword().equals(request.password())) {
            throw new BadRequestException("Invalid credentials");
        }

        return jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}