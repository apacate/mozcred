package com.ap.mozcred.controllers;

import com.ap.mozcred.dto.ClientDto;
import com.ap.mozcred.services.ClientService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        Page<ClientDto> clients = clientService.searchClients(search, status, page, size);
        model.addAttribute("clients", clients.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("searchQuery", search);
        model.addAttribute("status", status);
        model.addAttribute("totalPages", clients.getTotalPages());
        return "clients/index";
    }

    @GetMapping("/search")
    @ResponseBody
    public Page<ClientDto> searchClientsByName(@RequestParam String name,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        return clientService.searchClientsByName(name, page, size);
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

    @GetMapping("/detail/{id}")
    public String showClientDetails(@PathVariable Long id, Model model) {
        return clientService.findClientById(id)
                .map(client -> {
                    model.addAttribute("client", client);
                    return "clients/client-details";
                })
                .orElse("redirect:/clients");
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        return clientService.findClientById(id)
                .map(client -> {
                    model.addAttribute("clientDto", client);
                    return "clients/client-edit";
                })
                .orElse("redirect:/clients");
    }

    @PostMapping("/update")
    public String updateClient(@Valid @ModelAttribute ClientDto clientDto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "clients/client-edit";
        }
        try {
            clientService.updateClient(clientDto);
            redirectAttributes.addFlashAttribute("message", "Cliente atualizado com sucesso!");
            return "redirect:/clients";
        } catch (Exception e) {
            log.error("Erro ao atualizar cliente", e);
            model.addAttribute("error", "Erro ao atualizar cliente: " + e.getMessage());
            return "clients/client-edit";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            clientService.deleteClient(id);
            redirectAttributes.addFlashAttribute("successMessage", "Client deleted successfully!");
        } catch (Exception e) {
            log.error("Erro ao deletar cliente", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting client: " + e.getMessage());
        }
        return "redirect:/clients";
    }
}