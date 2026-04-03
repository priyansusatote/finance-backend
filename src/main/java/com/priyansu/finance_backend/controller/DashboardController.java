package com.priyansu.finance_backend.controller;

import com.priyansu.finance_backend.dto.DashboardResponse;
import com.priyansu.finance_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/whoami")
    public String whoami(Authentication auth) {
        return auth.getAuthorities().toString();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @GetMapping("/summary")
    public ResponseEntity<DashboardResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }
}
