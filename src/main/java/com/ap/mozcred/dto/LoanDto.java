package com.ap.mozcred.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDto {
    private Long id;

    @NotNull(message = "O valor do empréstimo é obrigatório")
    @Positive(message = "O valor do empréstimo deve ser positivo")
    private BigDecimal amount;

    @NotNull(message = "A data do empréstimo é obrigatória")
    private LocalDate date;

    @NotNull(message = "A taxa de juros é obrigatória")
    @Positive(message = "A taxa de juros deve ser positiva")
    private BigDecimal interestRate;

    @NotNull(message = "O tipo de amortização é obrigatório")
    private String amortizationType;

    @NotNull(message = "O ID do cliente é obrigatório")
    @Positive(message = "O ID do cliente deve ser válido")
    private Long clientId;

    private String clientName;

    @NotNull(message = "A frequência é obrigatória")
    private String frequency;

    @NotNull(message = "O número de parcelas é obrigatório")
    @Positive(message = "O número de parcelas deve ser positivo")
    private Integer numberOfInstallments;
}