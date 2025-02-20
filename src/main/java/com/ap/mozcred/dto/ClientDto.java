package com.ap.mozcred.dto;

import com.ap.mozcred.entities.Client.ClientStatus;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Número de telefone inválido")
    private String phone;

    @NotBlank(message = "O endereço é obrigatório")
    private String address;

    private ClientStatus status;

    private LocalDate createdAt;
}