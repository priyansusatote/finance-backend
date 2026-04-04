package com.priyansu.finance_backend.repository;

import com.priyansu.finance_backend.entity.FinancialRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord,Long> {

    List<FinancialRecord> findByDeletedFalse();

    Optional<FinancialRecord> findByIdAndDeletedFalse(Long id);
}
