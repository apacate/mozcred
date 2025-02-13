package com.mozcred.repositories;

import com.mozcred.entities.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(String email);
    Page<Client> findAllByOrderByIdDesc(PageRequest pageRequest);


    // Paginação de clientes, ordenando do mais recente para o mais antigo
    Page<Client> findAllByOrderByIdDesc(Pageable pageable);

    // Buscar clientes pelo primeiro nome (ignorando caixa alta/baixa)
    Page<Client> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);

    // Buscar clientes pelo e-mail (ignorando caixa alta/baixa)
    Page<Client> findByEmailContainingIgnoreCase(String email, Pageable pageable);

    // Buscar clientes pelo status (por exemplo: "New", "Inactive", etc.)
    Page<Client> findByStatusIgnoreCase(String status, Pageable pageable);

    // Buscar todos os clientes por status sem paginação
    List<Client> findByStatusIgnoreCase(String status);

    Page<Client> findByFirstNameContainingIgnoreCaseAndStatusIgnoreCase(String firstName, String status, Pageable pageable);
}
