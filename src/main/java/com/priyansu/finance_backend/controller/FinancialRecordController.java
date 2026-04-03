package com.priyansu.finance_backend.controller;


import com.priyansu.finance_backend.dto.FinancialRecordRequest;
import com.priyansu.finance_backend.dto.FinancialRecordResponse;
import com.priyansu.finance_backend.service.FinancialRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    //Admin-only
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<FinancialRecordResponse> create(
            @Valid @RequestBody FinancialRecordRequest request
    ) {
        return ResponseEntity.ok(recordService.createRecord(request));
    }

    //Admin + Analyst (read)
    @PreAuthorize("hasAnyRole('ADMIN' , 'ANALYST')")
    @GetMapping
    public ResponseEntity<List<FinancialRecordResponse>> getAll() {
        return ResponseEntity.ok(recordService.getAllRecords());
    }

    //Admin-only (delete)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }


}
