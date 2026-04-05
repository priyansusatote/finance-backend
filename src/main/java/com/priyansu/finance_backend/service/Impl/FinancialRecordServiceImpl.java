package com.priyansu.finance_backend.service.Impl;

import com.priyansu.finance_backend.dto.FinancialRecordRequest;
import com.priyansu.finance_backend.dto.FinancialRecordResponse;
import com.priyansu.finance_backend.entity.FinancialRecord;
import com.priyansu.finance_backend.entity.User;
import com.priyansu.finance_backend.enums.RecordType;
import com.priyansu.finance_backend.exception.BadRequestException;
import com.priyansu.finance_backend.exception.ResourceNotFoundException;
import com.priyansu.finance_backend.repository.FinancialRecordRepository;
import com.priyansu.finance_backend.repository.UserRepository;
import com.priyansu.finance_backend.security.JwtService;
import com.priyansu.finance_backend.service.FinancialRecordService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialRecordServiceImpl implements FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;


    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public FinancialRecordResponse createRecord(FinancialRecordRequest request) {

        String email = jwtService.getCurrentUserEmail();  //from securityContext
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResourceNotFoundException("Amount must be positive");
        }

        //for safe-enum handling
        RecordType type;
        try {
            type = RecordType.valueOf(request.type());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid record type");
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
        return recordRepository.findByDeletedFalse()  //find only non-deleted
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRecord(Long id) {
        FinancialRecord record = recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record not found or Already Deleted"));


        record.setDeleted(true); //soft-delete
        recordRepository.save(record);
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