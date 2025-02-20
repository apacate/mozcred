package com.ap.mozcred.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_installments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Installment {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 @Column(name = "installment_id", nullable = false)
 private Long id;

 @Column(name = "installment_number", nullable = false)
 private Integer installmentNumber;

 @Column(name = "installment_amount", nullable = false)
 private BigDecimal installmentAmount;

 @Column(name = "installment_interest", nullable = false)
 private BigDecimal installmentInterest;

 @Column(name = "installment_total", nullable = false)
 private BigDecimal installmentTotal;

 @Column(name = "installment_date", nullable = false)
 private LocalDate installmentDate;

 @Enumerated(EnumType.STRING)
 @Column(name = "installment_status", nullable = false)
 private InstallmentStatus installmentStatus;

 @ManyToOne
 @JoinColumn(name = "loan_id", nullable = false)
 private Loan loan;

 public enum InstallmentStatus {
  A_TEMPO, PENDENTE, VENCIDO, PAGO, CANCELADO
 }
}