package com.ap.mozcred.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class LoanWithInstallmentsDto {
    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private BigDecimal interestRate;
    private String amortizationType;
    private String frequency;
    private Long clientId;
    private String clientName;
    private List<InstallmentDto> installments;
}