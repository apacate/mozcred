package com.mozcred.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDto {

    private Long id;

    @NotBlank(message = "O primeiro nome é obrigatório")
    private String firstName;

    @NotBlank(message = "O sobrenome é obrigatório")
    private String lastName;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    private String phone;
    private String address;

    @NotBlank(message = "O status é obrigatório")
    private String status;

    private LocalDate createdAt;
}