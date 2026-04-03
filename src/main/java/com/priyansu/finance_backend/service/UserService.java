package com.priyansu.finance_backend.service;

import com.priyansu.finance_backend.dto.CreateUserRequest;
import com.priyansu.finance_backend.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    List<UserResponse> getAllUsers();

    void updateUserStatus(Long userId, String status);

    void updateUserRole(Long id, String role);
}
