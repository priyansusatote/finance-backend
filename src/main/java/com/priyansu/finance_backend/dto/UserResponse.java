package com.priyansu.finance_backend.dto;

public record UserResponse(

        Long id,
        String name,
        String email,
        String role,
        String status
) {
}
