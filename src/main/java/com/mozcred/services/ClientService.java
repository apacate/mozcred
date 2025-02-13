package com.mozcred.services;

import com.mozcred.dto.ClientDto;
import com.mozcred.entities.Client;
import com.mozcred.repositories.ClientRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    // Buscar todos os clientes com paginação
    public Page<ClientDto> getAllClients(int page, int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        return clientRepository.findAllByOrderByIdDesc(pageable)
                .map(this::convertToDto);
    }

    // Buscar cliente pelo ID
    public Optional<ClientDto> findClientById(Long id) {
        return clientRepository.findById(id).map(this::convertToDto);
    }

    // Buscar cliente pelo e-mail
    public Optional<ClientDto> findClientByEmail(String email) {
        return clientRepository.findByEmail(email)
                .map(this::convertToDto);
    }

    // Buscar clientes pelo nome (ignorando maiúsculas/minúsculas)
    public Page<ClientDto> searchClientsByName(String name, int page, int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        return clientRepository.findByFirstNameContainingIgnoreCase(name, pageable)
                .map(this::convertToDto);
    }




    // Buscar clientes pelo status
    public Page<ClientDto> findClientsByStatus(String status, int page, int size) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        return clientRepository.findByStatusIgnoreCase(status, pageable)
                .map(this::convertToDto);
    }


    public Page<ClientDto> searchClients(String search, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Combina nome e status se ambos forem fornecidos
        if (search != null && !search.isEmpty() && status != null && !status.isEmpty()) {
            return clientRepository
                    .findByFirstNameContainingIgnoreCaseAndStatusIgnoreCase(search, status, pageable)
                    .map(this::convertToDto);
        }

        // Pesquisa apenas pelo nome
        if (search != null && !search.isEmpty()) {
            return clientRepository
                    .findByFirstNameContainingIgnoreCase(search, pageable)
                    .map(this::convertToDto);
        }

        // Filtra apenas pelo status
        if (status != null && !status.isEmpty()) {
            return clientRepository
                    .findByStatusIgnoreCase(status, pageable)
                    .map(this::convertToDto);
        }

        // Retorna todos os clientes (sem filtros)
        return clientRepository.findAllByOrderByIdDesc(pageable).map(this::convertToDto);
    }




    // Criar novo cliente
    @Transactional
    public ClientDto saveClient(ClientDto clientDto) {
        if (clientRepository.findByEmail(clientDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Cliente com este e-mail já existe");
        }

        Client client = convertToEntity(clientDto);
        Client savedClient = clientRepository.save(client);
        log.info("Cliente salvo com sucesso: {}", savedClient.getEmail());

        return convertToDto(savedClient);
    }

    // Atualizar um cliente existente
    @Transactional
    public ClientDto updateClient(ClientDto clientDto) {
        Optional<Client> existingClient = clientRepository.findById(clientDto.getId());

        if (existingClient.isEmpty()) {
            throw new IllegalArgumentException("Cliente não encontrado.");
        }

        Client client = convertToEntity(clientDto);
        Client updatedClient = clientRepository.save(client);
        log.info("Cliente atualizado com sucesso: {}", updatedClient.getEmail());

        return convertToDto(updatedClient);
    }

    // Deletar um cliente pelo ID
    @Transactional
    public void deleteClient(Long id) {
        Optional<Client> clientOptional = clientRepository.findById(id);

        if (clientOptional.isPresent()) {
            clientRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Client with ID " + id + " not found.");
        }
    }




    // Conversão de entidade para DTO
    private ClientDto convertToDto(Client client) {
        return ClientDto.builder()
                .id(client.getId())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .address(client.getAddress())
                .status(client.getStatus())
                .createdAt(client.getCreatedAt())
                .build();
    }

    // Conversão de DTO para entidade
    private Client convertToEntity(ClientDto clientDto) {
        return Client.builder()
                .id(clientDto.getId())
                .firstName(clientDto.getFirstName())
                .lastName(clientDto.getLastName())
                .email(clientDto.getEmail())
                .phone(clientDto.getPhone())
                .address(clientDto.getAddress())
                .status(clientDto.getStatus())
                .build();
    }


}
