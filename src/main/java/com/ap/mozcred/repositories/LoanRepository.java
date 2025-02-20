package com.ap.mozcred.repositories;

import com.ap.mozcred.entities.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query("SELECT l FROM Loan l JOIN FETCH l.client WHERE l.client IS NOT NULL")
    List<Loan> findAllWithClients();

    List<Loan> findByClientId(Long clientId);
}