package com.priyansu.finance_backend.service.Impl;

import com.priyansu.finance_backend.dto.DashboardResponse;
import com.priyansu.finance_backend.dto.FinancialRecordResponse;
import com.priyansu.finance_backend.entity.FinancialRecord;
import com.priyansu.finance_backend.enums.RecordType;
import com.priyansu.finance_backend.repository.FinancialRecordRepository;
import com.priyansu.finance_backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final FinancialRecordRepository recordRepository;

    @Override
    public DashboardResponse getSummary() {

        List<FinancialRecord> records = recordRepository.findByDeletedFalse();

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        Map<String, BigDecimal> categoryMap = new HashMap<>();

        for (FinancialRecord record : records) {

            //Income/Expense
            if (record.getType() == RecordType.INCOME) {
                totalIncome = totalIncome.add(record.getAmount());
            } else {
                totalExpense = totalExpense.add(record.getAmount());
            }

            //category-Wise ("food"-2000, "salary"-8000 ,...)
            categoryMap.put(
                    record.getCategory(),
                    categoryMap.getOrDefault(record.getCategory(), BigDecimal.ZERO)
                            .add(record.getAmount())
            );
        }

        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        // Recent Transactions (last 5)
        List<FinancialRecordResponse> recent = records.stream()
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .limit(5)
                .map(this::mapToResponse)
                .toList();

        return new DashboardResponse(
                totalIncome,
                totalExpense,
                netBalance,
                categoryMap,
                recent
        );
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
