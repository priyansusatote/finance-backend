package com.priyansu.finance_backend.service.Impl;

import com.priyansu.finance_backend.dto.CreateUserRequest;
import com.priyansu.finance_backend.dto.UserResponse;
import com.priyansu.finance_backend.entity.User;
import com.priyansu.finance_backend.enums.Role;
import com.priyansu.finance_backend.enums.UserStatus;
import com.priyansu.finance_backend.repository.UserRepository;
import com.priyansu.finance_backend.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email().toLowerCase())
                .password(request.password()) //later will hash by Bcrypt
                .role(Role.VIEWER)  //Default new user
                .status(UserStatus.ACTIVE)
                .build();

        User saved = userRepository.save(user);

        return mapToResponse(saved);
    }


    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)  //user -> mapToResponse(user)
                .toList();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateUserStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //for Safe ENUM conversion
        try {
            user.setStatus(UserStatus.valueOf(status));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status value");
        }

        userRepository.save(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void updateUserRole(Long userId, String role) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            user.setRole(Role.valueOf(role));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role value");
        }

        userRepository.save(user);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name()
        );
    }
}
