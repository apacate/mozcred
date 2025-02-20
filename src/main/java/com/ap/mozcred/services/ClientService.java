package com.ap.mozcred.services;

import com.ap.mozcred.dto.ClientDto;
import com.ap.mozcred.entities.Client;
import com.ap.mozcred.repositories.ClientRepository;
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

    public Page<ClientDto> getAllClients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return clientRepository.findAllByOrderByIdDesc(pageable)
                .map(this::convertToDto);
    }

    public Optional<ClientDto> findClientById(Long id) {
        return clientRepository.findById(id).map(this::convertToDto);
    }

    public Page<ClientDto> searchClientsByName(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return clientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, pageable)
                .map(this::convertToDto);
    }

    public Optional<ClientDto> findClientByEmail(String email) {
        return clientRepository.findByEmail(email).map(this::convertToDto);
    }

    public Page<ClientDto> searchClients(String search, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (search != null && !search.isEmpty() && status != null && !status.isEmpty()) {
            return clientRepository.findByFirstNameContainingIgnoreCaseAndStatus(search, status, pageable)
                    .map(this::convertToDto);
        }
        if (search != null && !search.isEmpty()) {
            return clientRepository.findByFirstNameContainingIgnoreCase(search, pageable)
                    .map(this::convertToDto);
        }
        if (status != null && !status.isEmpty()) {
            return clientRepository.findByStatusIgnoreCase(status, pageable)
                    .map(this::convertToDto);
        }
        return clientRepository.findAllByOrderByIdDesc(pageable).map(this::convertToDto);
    }

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

    @Transactional
    public void deleteClient(Long id) {
        Optional<Client> clientOptional = clientRepository.findById(id);
        if (clientOptional.isPresent()) {
            clientRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Client with ID " + id + " not found.");
        }
    }

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