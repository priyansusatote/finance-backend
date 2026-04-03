package com.priyansu.finance_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialRecordResponse(

        Long id,
        BigDecimal amount,
        String type,
        String category,
        LocalDate date,
        String note
) {}