package com.priyansu.finance_backend.repository;

import com.priyansu.finance_backend.entity.FinancialRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord,Long> {
}
