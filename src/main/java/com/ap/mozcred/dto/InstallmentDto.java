package com.ap.mozcred.dto;

import com.ap.mozcred.entities.Installment.InstallmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstallmentDto {
    private Long id;

    @NotNull(message = "O número da parcela é obrigatório")
    @Positive(message = "O número da parcela deve ser positivo")
    private Integer installmentNumber;

    @NotNull(message = "O valor da parcela é obrigatório")
    @Positive(message = "O valor da parcela deve ser positivo")
    private BigDecimal installmentAmount;

    @NotNull(message = "O valor dos juros é obrigatório")
    @Positive(message = "O valor dos juros deve ser positivo")
    private BigDecimal installmentInterest;

    @NotNull(message = "O valor total é obrigatório")
    @Positive(message = "O valor total deve ser positivo")
    private BigDecimal installmentTotal;

    @NotNull(message = "A data da parcela é obrigatória")
    private LocalDate installmentDate;

    @NotNull(message = "O status da parcela é obrigatório")
    private InstallmentStatus installmentStatus;

    @NotNull(message = "O ID do empréstimo é obrigatório")
    @Positive(message = "O ID do empréstimo deve ser positivo")
    private Long loanId;

    private long daysDifference; // Novo campo para diferença de dias
}