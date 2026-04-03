package com.priyansu.finance_backend.entity;

import com.priyansu.finance_backend.enums.RecordType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "financial_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;  //BigDecimal :to ensure accurate financial calculations

    @Enumerated(EnumType.STRING)
    private RecordType type;

    private String category;

    private LocalDate date;

    private String note;

    @ManyToOne //Many records can be belong to one user
    @JoinColumn(name = "user_id")
    private User createdBy;
}
