package com.ap.mozcred.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    private Long id;

    @NotBlank(message = "O primeiro nome é obrigatório")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "O sobrenome é obrigatório")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Email(message = "Email inválido")
    @Column(unique = true, nullable = false)
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Número de telefone inválido")
    @Column(name = "phone", nullable = false)
    private String phone;

    @NotBlank(message = "O endereço é obrigatório")
    @Column(name = "address", nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Loan> loans = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
    }

    public enum ClientStatus {
        ATIVO, INATIVO, BLOQUEADO
    }
}