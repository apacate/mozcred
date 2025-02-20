package com.ap.mozcred.controllers;

import com.ap.mozcred.dto.ClientDto;
import com.ap.mozcred.dto.InstallmentDto;
import com.ap.mozcred.dto.LoanDto;
import com.ap.mozcred.dto.LoanWithInstallmentsDto;
import com.ap.mozcred.services.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/loans")
@Slf4j
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @GetMapping
    public String listLoans(@PageableDefault(size = 10, sort = "id") Pageable pageable, Model model) {
        log.info("Listing loans with pagination: {}", pageable);
        Page<LoanDto> loans = loanService.findAll(pageable);
        model.addAttribute("loans", loans);
        return "loans/loan-list";
    }

    @GetMapping("/{loanId}/installments")
    public String getInstallments(@PathVariable Long loanId, Model model) {
        log.info("Fetching loan details and installments for loan ID: {}", loanId);
        try {
            LoanWithInstallmentsDto loanDetails = loanService.getLoanDetailsWithInstallments(loanId);
            LocalDate currentDate = LocalDate.now();
            // Calcular a diferença de dias para cada prestação
            for (InstallmentDto installment : loanDetails.getInstallments()) {
                long daysDifference = ChronoUnit.DAYS.between(currentDate, installment.getInstallmentDate());
                installment.setDaysDifference(daysDifference); // Adicionar ao InstallmentDto
            }
            model.addAttribute("loanDetails", loanDetails);
            model.addAttribute("currentDate", currentDate);
            return "loans/loan-details";
        } catch (IllegalArgumentException e) {
            log.error("Loan not found with ID: {}", loanId, e);
            model.addAttribute("error", "Empréstimo não encontrado.");
            return "loans/loan-list";
        }
    }

    @GetMapping("/new")
    public String showLoanForm(Model model) {
        log.info("Showing loan creation form");
        model.addAttribute("loanDto", new LoanDto());
        return "loans/loan-form";
    }

    @PostMapping
    public String handleLoanForm(@Valid @ModelAttribute LoanDto loanDto,
                                 BindingResult bindingResult,
                                 @RequestParam("action") String action,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        log.info("Handling loan form with action: {}, data: {}", action, loanDto);
        if (bindingResult.hasErrors()) {
            return "loans/loan-form";
        }
        try {
            if ("save".equals(action)) {
                loanService.criarEmprestimo(loanDto);
                redirectAttributes.addFlashAttribute("successMessage", "Empréstimo criado com sucesso!");
                return "redirect:/loans";
            } else if ("calculate".equals(action)) {
                List<InstallmentDto> installments = loanService.calcularAmortizacao(loanDto);
                BigDecimal totalInterest = installments.stream()
                        .map(InstallmentDto::getInstallmentInterest)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalAmount = installments.stream()
                        .map(InstallmentDto::getInstallmentTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                model.addAttribute("loanDto", loanDto);
                model.addAttribute("installments", installments);
                model.addAttribute("totalInterest", totalInterest);
                model.addAttribute("totalAmount", totalAmount);
                return "loans/loan-form";
            }
            model.addAttribute("error", "Ação inválida.");
            return "loans/loan-form";
        } catch (IllegalArgumentException e) {
            log.error("Error processing loan form: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "loans/loan-form";
        }
    }

    @GetMapping("/clients/search")
    public String searchClients(@RequestParam String name,
                                @PageableDefault(size = 10) Pageable pageable,
                                Model model) {
        log.info("Searching clients by name: {} with pagination: {}", name, pageable);
        Page<ClientDto> clients = loanService.searchClientsByName(name, pageable);
        model.addAttribute("clients", clients);
        return "loans/client-search";
    }
}