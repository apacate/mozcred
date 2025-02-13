package com.mozcred.controllers;

import com.mozcred.dto.ClientDto;
import com.mozcred.services.ClientService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/clients")
@Slf4j
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }



    @GetMapping
    public String getClients(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "8") int size,
                             @RequestParam(required = false) String search,
                             @RequestParam(required = false) String status,
                             Model model) {
        Page<ClientDto> clients;

        if ((search != null && !search.isEmpty()) || (status != null && !status.isEmpty())) {
            // Buscar clientes com base nos filtros de pesquisa
            clients = clientService.searchClients(search, status, page, size);
        } else {
            // Retornar todos os clientes se não houver filtros
            clients = clientService.getAllClients(page, size);
        }

        model.addAttribute("clients", clients.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("searchQuery", search);
        model.addAttribute("status", status);
        model.addAttribute("totalPages", clients.getTotalPages());
        return "clients/index"; // Caminho para o template Thymeleaf
    }


    @GetMapping("/create")
    public String showCreateClientForm(Model model) {
        model.addAttribute("clientDto", new ClientDto());
        return "clients/create";
    }

    @PostMapping("/create")
    public String saveClient(@Valid @ModelAttribute ClientDto clientDto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (bindingResult.hasErrors()) {
            return "clients/create";
        }

        try {
            clientService.saveClient(clientDto);
            redirectAttributes.addFlashAttribute("message", "Cliente cadastrado com sucesso!");
            return "redirect:/clients";
        } catch (IllegalArgumentException e) {
            log.warn("Erro ao salvar cliente: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "clients/create";
        } catch (Exception e) {
            log.error("Erro inesperado ao salvar cliente", e);
            model.addAttribute("error", "Ocorreu um erro inesperado. Tente novamente mais tarde.");
            return "clients/create";
        }
    }



    // Exibir detalhes do cliente
    @GetMapping("/detail/{id}")
    public String showClientDetails(@PathVariable Long id, Model model) {
        Optional<ClientDto> client = clientService.findClientById(id);
        if (client.isPresent()) {
            model.addAttribute("client", client.get());
            return "clients/client-details";
        }
        return "redirect:/clients";
    }

    // Exibir formulário de edição
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<ClientDto> client = clientService.findClientById(id);
        if (client.isPresent()) {
            model.addAttribute("clientDto", client.get());
            return "clients/client-edit";
        }
        return "redirect:/clients";
    }

    // Atualizar cliente
    @PostMapping("/update")
    public String updateClient(@Valid @ModelAttribute ClientDto clientDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "clients/client-edit";
        }

        try {
            clientService.updateClient(clientDto);
            redirectAttributes.addFlashAttribute("message", "Cliente atualizado com sucesso!");
            return "redirect:/clients";
        } catch (Exception e) {
            log.error("Erro ao atualizar cliente", e);
            redirectAttributes.addFlashAttribute("error", "Erro ao atualizar cliente.");
            return "clients/client-edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clientService.deleteClient(id);
            redirectAttributes.addFlashAttribute("successMessage", "Client deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting client: " + e.getMessage());
        }
        return "redirect:/clients";
    }


}
