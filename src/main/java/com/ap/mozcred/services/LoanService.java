package com.ap.mozcred.services;

import com.ap.mozcred.dto.ClientDto;
import com.ap.mozcred.dto.InstallmentDto;
import com.ap.mozcred.dto.LoanDto;
import com.ap.mozcred.dto.LoanWithInstallmentsDto;
import com.ap.mozcred.entities.Client;
import com.ap.mozcred.entities.Installment;
import com.ap.mozcred.entities.Loan;
import com.ap.mozcred.repositories.ClientRepository;
import com.ap.mozcred.repositories.LoanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final LoanRepository loanRepository;
    private final ClientRepository clientRepository;

    public Page<LoanDto> findAll(Pageable pageable) {
        return loanRepository.findAll(pageable).map(this::mapToLoanDto);
    }

    public Loan findLoanById(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found with ID: " + loanId));
    }


    @Transactional
    public LoanWithInstallmentsDto getLoanDetailsWithInstallments(Long loanId) {
        logger.info("Fetching loan details with ID: {}", loanId);
        Loan loan = findLoanById(loanId);
        LocalDate currentDate = LocalDate.now();

        boolean updated = false;
        for (Installment installment : loan.getInstallments()) {
            if (installment.getInstallmentStatus() == Installment.InstallmentStatus.A_TEMPO) {
                if (currentDate.isAfter(installment.getInstallmentDate())) {
                    installment.setInstallmentStatus(Installment.InstallmentStatus.VENCIDO);
                    logger.info("Updated installment ID: {} from A_TEMPO to VENCIDO", installment.getId());
                    updated = true;
                } else if (currentDate.isEqual(installment.getInstallmentDate())) {
                    installment.setInstallmentStatus(Installment.InstallmentStatus.PENDENTE);
                    logger.info("Updated installment ID: {} from A_TEMPO to PENDENTE", installment.getId());
                    updated = true;
                }
            }
        }
        if (updated) {
            loanRepository.save(loan);
        }

        List<InstallmentDto> installmentDtos = loan.getInstallments().stream()
                .map(this::mapToInstallmentDto)
                .toList();
        logger.info("Fetched {} installments for loan ID: {}", installmentDtos.size(), loanId);
        installmentDtos.forEach(dto -> logger.info("Installment ID: {}, Status: {}", dto.getId(), dto.getInstallmentStatus()));
        return LoanWithInstallmentsDto.builder()
                .id(loan.getId())
                .amount(loan.getAmount())
                .date(loan.getDate())
                .interestRate(loan.getInterestRate())
                .amortizationType(loan.getAmortizationType().name())
                .frequency(loan.getFrequency().name())
                .clientId(loan.getClient().getId())
                .clientName(loan.getClient().getFirstName() + " " + loan.getClient().getLastName())
                .installments(installmentDtos)
                .build();
    }

    private InstallmentDto mapToInstallmentDto(Installment installment) {
        Installment.InstallmentStatus status = installment.getInstallmentStatus() != null ?
                installment.getInstallmentStatus() : Installment.InstallmentStatus.A_TEMPO;
        logger.info("Mapping installment ID: {}, Status from DB: {}, Assigned Status: {}",
                installment.getId(), installment.getInstallmentStatus(), status);
        return InstallmentDto.builder()
                .id(installment.getId())
                .installmentNumber(installment.getInstallmentNumber())
                .installmentAmount(installment.getInstallmentAmount())
                .installmentInterest(installment.getInstallmentInterest())
                .installmentTotal(installment.getInstallmentTotal())
                .installmentDate(installment.getInstallmentDate())
                .installmentStatus(status)
                .loanId(installment.getLoan().getId())
                .daysDifference(ChronoUnit.DAYS.between(LocalDate.now(), installment.getInstallmentDate()))
                .build();
    }


    @Transactional
    public LoanDto criarEmprestimo(LoanDto loanDto) {
        logger.info("Creating loan with data: {}", loanDto);
        validarParametros(loanDto);

        Client client = clientRepository.findById(loanDto.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found with ID: " + loanDto.getClientId()));

        Loan loan = buildLoanFromDto(loanDto, client);
        Loan savedLoan = loanRepository.save(loan);

        List<InstallmentDto> installments = calcularAmortizacao(loanDto);
        savedLoan.getInstallments().addAll(buildInstallmentsFromDtos(installments, savedLoan));
        savedLoan = loanRepository.save(savedLoan);

        logger.info("Loan created successfully with ID: {}", savedLoan.getId());
        return mapToLoanDto(savedLoan);
    }


    private Loan buildLoanFromDto(LoanDto loanDto, Client client) {
        return Loan.builder()
                .amount(loanDto.getAmount())
                .date(loanDto.getDate())
                .interestRate(loanDto.getInterestRate())
                .amortizationType(Loan.AmortizationType.valueOf(loanDto.getAmortizationType()))
                .frequency(Loan.Frequency.valueOf(loanDto.getFrequency()))
                .client(client)
                .build();
    }

    private List<Installment> buildInstallmentsFromDtos(List<InstallmentDto> dtos, Loan loan) {
        return dtos.stream()
                .map(dto -> {
                    Installment installment = Installment.builder()
                            .installmentNumber(dto.getInstallmentNumber())
                            .installmentAmount(dto.getInstallmentAmount())
                            .installmentInterest(dto.getInstallmentInterest())
                            .installmentTotal(dto.getInstallmentTotal())
                            .installmentDate(dto.getInstallmentDate())
                            .installmentStatus(Installment.InstallmentStatus.A_TEMPO) // Sempre A_TEMPO na criação
                            .loan(loan)
                            .build();
                    logger.info("Created installment with status: {}", installment.getInstallmentStatus());
                    return installment;
                })
                .toList();
    }

    private LoanDto mapToLoanDto(Loan loan) {
        return LoanDto.builder()
                .id(loan.getId())
                .amount(loan.getAmount())
                .date(loan.getDate())
                .interestRate(loan.getInterestRate())
                .amortizationType(loan.getAmortizationType().name())
                .frequency(loan.getFrequency().name())
                .clientId(loan.getClient().getId())
                .clientName(loan.getClient().getFirstName() + " " + loan.getClient().getLastName())
                .numberOfInstallments(loan.getInstallments().size())
                .build();
    }


    public Page<ClientDto> searchClientsByName(String name, Pageable pageable) {
        return clientRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, pageable)
                .map(client -> ClientDto.builder()
                        .id(client.getId())
                        .firstName(client.getFirstName())
                        .lastName(client.getLastName())
                        .email(client.getEmail())
                        .phone(client.getPhone())
                        .address(client.getAddress())
                        .status(client.getStatus())
                        .createdAt(client.getCreatedAt())
                        .build());
    }




    public List<InstallmentDto> calcularAmortizacao(LoanDto loanDto) {
        validarParametros(loanDto);
        BigDecimal proportionalRate = calcularTaxaProporcional(loanDto.getInterestRate(), loanDto.getFrequency());

        return switch (loanDto.getAmortizationType()) {
            case "PRESTACOES_CONSTANTES" -> calcularPrestacoesConstantes(loanDto, proportionalRate);
            case "CAPITAL_CONSTANTE" -> calcularCapitalConstante(loanDto, proportionalRate);
            default -> throw new IllegalArgumentException("Invalid amortization type: " + loanDto.getAmortizationType());
        };
    }


    private List<InstallmentDto> calcularPrestacoesConstantes(LoanDto loanDto, BigDecimal proportionalRate) {
        BigDecimal factor = proportionalRate.add(BigDecimal.ONE).pow(loanDto.getNumberOfInstallments());
        BigDecimal fixedInstallment = loanDto.getAmount().multiply(proportionalRate).multiply(factor)
                .divide(factor.subtract(BigDecimal.ONE), SCALE, ROUNDING_MODE);

        List<InstallmentDto> installments = new ArrayList<>();
        BigDecimal remainingBalance = loanDto.getAmount();

        for (int i = 0; i < loanDto.getNumberOfInstallments(); i++) {
            BigDecimal interest = remainingBalance.multiply(proportionalRate).setScale(SCALE, ROUNDING_MODE);
            BigDecimal principal = fixedInstallment.subtract(interest).setScale(SCALE, ROUNDING_MODE);
            remainingBalance = remainingBalance.subtract(principal).setScale(SCALE, ROUNDING_MODE);

            LocalDate installmentDate = calcularDataParcela(loanDto.getDate(), i, loanDto.getFrequency());
            installments.add(createInstallmentDto(i + 1, installmentDate, principal, interest, fixedInstallment));
        }
        return installments;
    }

    private List<InstallmentDto> calcularCapitalConstante(LoanDto loanDto, BigDecimal proportionalRate) {
        BigDecimal constantPrincipal = loanDto.getAmount()
                .divide(BigDecimal.valueOf(loanDto.getNumberOfInstallments()), SCALE, ROUNDING_MODE);

        List<InstallmentDto> installments = new ArrayList<>();
        BigDecimal remainingBalance = loanDto.getAmount();

        for (int i = 0; i < loanDto.getNumberOfInstallments(); i++) {
            BigDecimal interest = remainingBalance.multiply(proportionalRate).setScale(SCALE, ROUNDING_MODE);
            BigDecimal total = constantPrincipal.add(interest).setScale(SCALE, ROUNDING_MODE);
            remainingBalance = remainingBalance.subtract(constantPrincipal).setScale(SCALE, ROUNDING_MODE);

            LocalDate installmentDate = calcularDataParcela(loanDto.getDate(), i, loanDto.getFrequency());
            installments.add(createInstallmentDto(i + 1, installmentDate, constantPrincipal, interest, total));
        }
        return installments;
    }

    private InstallmentDto createInstallmentDto(int number, LocalDate date, BigDecimal principal, BigDecimal interest, BigDecimal total) {
        return InstallmentDto.builder()
                .installmentNumber(number)
                .installmentDate(date)
                .installmentAmount(principal)
                .installmentInterest(interest)
                .installmentTotal(total)
                .installmentStatus(Installment.InstallmentStatus.A_TEMPO) // Sempre A_TEMPO na criação
                .build();
    }

    public void validarParametros(LoanDto loanDto) {
        if (loanDto.getAmount() == null || loanDto.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Loan amount must be positive.");
        }
        if (loanDto.getDate() == null || loanDto.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Loan date must be today or in the past.");
        }
        if (loanDto.getNumberOfInstallments() == null || loanDto.getNumberOfInstallments() <= 0) {
            throw new IllegalArgumentException("Number of installments must be greater than 0.");
        }
        if (loanDto.getInterestRate() == null || loanDto.getInterestRate().signum() <= 0) {
            throw new IllegalArgumentException("Interest rate must be positive.");
        }
        if (!List.of("CAPITAL_CONSTANTE", "PRESTACOES_CONSTANTES").contains(loanDto.getAmortizationType())) {
            throw new IllegalArgumentException("Invalid amortization type: " + loanDto.getAmortizationType());
        }
    }

    private BigDecimal calcularTaxaProporcional(BigDecimal annualRate, String frequency) {
        return switch (frequency.toLowerCase()) {
            case "mensal" -> annualRate.divide(BigDecimal.valueOf(1), 10, ROUNDING_MODE);
            case "quinzenal" -> annualRate.divide(BigDecimal.valueOf(2), 10, ROUNDING_MODE);
            case "semanal" -> annualRate.divide(BigDecimal.valueOf(4), 10, ROUNDING_MODE);
            case "diario" -> annualRate.divide(BigDecimal.valueOf(30), 10, ROUNDING_MODE);
            default -> throw new IllegalArgumentException("Invalid frequency: " + frequency);
        };
    }

    private LocalDate calcularDataParcela(LocalDate initialDate, int installmentNumber, String frequency) {
        return switch (frequency.toLowerCase()) {
            case "mensal" -> initialDate.plusMonths(installmentNumber);
            case "quinzenal" -> initialDate.plusWeeks(installmentNumber * 2);
            case "semanal" -> initialDate.plusWeeks(installmentNumber);
            case "diario" -> initialDate.plusDays(installmentNumber);
            default -> throw new IllegalArgumentException("Invalid frequency: " + frequency);
        };
    }
}