package com.priyansu.finance_backend.service.Impl;

import com.priyansu.finance_backend.dto.FinancialRecordRequest;
import com.priyansu.finance_backend.dto.FinancialRecordResponse;
import com.priyansu.finance_backend.entity.FinancialRecord;
import com.priyansu.finance_backend.entity.User;
import com.priyansu.finance_backend.enums.RecordType;
import com.priyansu.finance_backend.repository.FinancialRecordRepository;
import com.priyansu.finance_backend.repository.UserRepository;
import com.priyansu.finance_backend.service.FinancialRecordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialRecordServiceImpl implements FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public FinancialRecordResponse createRecord(FinancialRecordRequest request) {

        // TEMP: assign dummy user (later from auth)
        User user = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be positive");
        }

        //for safe-enum handling
        RecordType type;
        try {
            type = RecordType.valueOf(request.type());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid record type");
        }

        FinancialRecord record = FinancialRecord.builder()
                .amount(request.amount())
                .type(type)
                .category(request.category())
                .date(request.date() != null ? request.date() : LocalDate.now())
                .note(request.note())
                .createdBy(user)
                .build();

        FinancialRecord saved = recordRepository.save(record);

        return mapToResponse(saved);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN' , 'ANALYST')")
    public List<FinancialRecordResponse> getAllRecords() {
        return recordRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRecord(Long id) {
        FinancialRecord record = recordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        recordRepository.delete(record);
    }

    private FinancialRecordResponse mapToResponse(FinancialRecord record) {
        return new FinancialRecordResponse(
                record.getId(),
                record.getAmount(),
                record.getType().name(),
                record.getCategory(),
                record.getDate(),
                record.getNote()
        );
    }
}