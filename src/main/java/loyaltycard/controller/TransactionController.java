package loyaltycard.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import loyaltycard.controller.dto.CreateTransactionRequestDto;
import loyaltycard.controller.dto.TransactionDto;
import loyaltycard.mapper.TransactionMapper;
import loyaltycard.service.TransactionService;
import loyaltycard.service.model.Direction;
import loyaltycard.service.model.Transaction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cards/{cardId}/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @Secured("ADMIN")
    @PostMapping
    public ResponseEntity<TransactionDto> createTransaction(
            @PathVariable UUID cardId,
            @Valid @RequestBody CreateTransactionRequestDto requestDto,
            @AuthenticationPrincipal UserDetails principal) {

        UUID adminId = UUID.fromString(principal.getUsername());

        Transaction transaction = new Transaction();
        transaction.setDirection(Direction.valueOf(requestDto.getDirection().name()));
        transaction.setAmount(requestDto.getAmount());
        transaction.setNote(requestDto.getNote());

        Transaction created = transactionService.createTransaction(cardId, transaction, adminId);
        TransactionDto transactionDto = transactionMapper.toDto(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(transactionDto);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN') or @cardServiceImpl.isCardOwner(#cardId, authentication.name)")
    @GetMapping
    public ResponseEntity<List<TransactionDto>> getTransactions(@PathVariable UUID cardId) {

        List<Transaction> transactions = transactionService.getTransactions(cardId);
        List<TransactionDto> transactionDtos = transactions.stream()
                .map(transactionMapper::toDto)
                .toList();

        return ResponseEntity.ok().body(transactionDtos);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN') or @cardServiceImpl.isCardOwner(#cardId, authentication.name)")
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionDto> getTransactionById(
            @PathVariable UUID cardId,
            @PathVariable UUID transactionId) {

        Transaction transaction = transactionService.getTransactionById(cardId, transactionId);
        TransactionDto transactionDto = transactionMapper.toDto(transaction);

        return ResponseEntity.ok().body(transactionDto);
    }

    @Secured("ADMIN")
    @PutMapping("/{transactionId}/cancel")
    public ResponseEntity<TransactionDto> cancelTransaction(
            @PathVariable UUID cardId,
            @PathVariable UUID transactionId) {

        Transaction transaction = transactionService.cancelTransaction(cardId, transactionId);
        TransactionDto transactionDto = transactionMapper.toDto(transaction);

        return ResponseEntity.ok().body(transactionDto);
    }
}
