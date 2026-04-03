package com.priyansu.finance_backend.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardResponse(

        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal netBalance,

        Map<String, BigDecimal> categoryWise,

        List<FinancialRecordResponse> recentTransactions
) {
}
