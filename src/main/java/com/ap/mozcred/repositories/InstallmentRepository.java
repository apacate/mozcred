package com.ap.mozcred.repositories;

import com.ap.mozcred.entities.Installment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstallmentRepository extends JpaRepository<Installment, Long> {
 List<Installment> findByLoanId(Long loanId);
}