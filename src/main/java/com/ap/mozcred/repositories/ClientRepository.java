package com.ap.mozcred.repositories;

import com.ap.mozcred.entities.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByEmail(String email);

    Page<Client> findAllByOrderByIdDesc(Pageable pageable);

    Page<Client> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);

    @Query("SELECT c FROM Client c WHERE LOWER(c.status) = LOWER(:status)")
    Page<Client> findByStatusIgnoreCase(@Param("status") String status, Pageable pageable);

    @Query("SELECT c FROM Client c WHERE LOWER(c.firstName) LIKE LOWER(concat('%', :searchTerm, '%')) OR LOWER(c.lastName) LIKE LOWER(concat('%', :searchTerm, '%'))")
    Page<Client> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT c FROM Client c WHERE LOWER(c.firstName) LIKE LOWER(concat('%', :search, '%')) AND LOWER(c.status) = LOWER(:status)")
    Page<Client> findByFirstNameContainingIgnoreCaseAndStatus(@Param("search") String search, @Param("status") String status, Pageable pageable);
}