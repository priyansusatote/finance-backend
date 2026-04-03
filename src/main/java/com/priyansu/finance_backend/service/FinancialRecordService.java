package com.priyansu.finance_backend.service;

import com.priyansu.finance_backend.dto.FinancialRecordRequest;
import com.priyansu.finance_backend.dto.FinancialRecordResponse;

import java.util.List;

public interface FinancialRecordService {

    FinancialRecordResponse createRecord(FinancialRecordRequest request);

    List<FinancialRecordResponse> getAllRecords();

    void deleteRecord(Long id);
}