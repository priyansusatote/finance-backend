package com.priyansu.finance_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FinancialRecordRequest(

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotNull(message = "Type is required")
        String type, //String is used  for flexibility, but converted to ENUM internally for type safety: like later in service :RecordType.valueOf(request.type())

        String category,

        LocalDate date,

        String note
) {
}
