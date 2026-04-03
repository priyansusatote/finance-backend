package com.priyansu.finance_backend.controller;

import com.priyansu.finance_backend.dto.CreateUserRequest;
import com.priyansu.finance_backend.dto.UserResponse;
import com.priyansu.finance_backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //Public - signup
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {

        return ResponseEntity.ok(userService.createUser(request));
    }

    //Admin-only-Access
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    //Admin-only
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable long id,
            @RequestParam String status
    ) {
        userService.updateUserStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    //role-update (admin-only)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> updateRole(
            @PathVariable Long id,
            @RequestParam String role) {

        userService.updateUserRole(id, role);
        return ResponseEntity.noContent().build();
    }
}
