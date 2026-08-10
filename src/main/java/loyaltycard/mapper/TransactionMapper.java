package loyaltycard.mapper;

import loyaltycard.controller.dto.DirectionDto;
import loyaltycard.controller.dto.TransactionDto;
import loyaltycard.controller.dto.TransactionStatusDto;
import loyaltycard.repository.entity.DirectionEntity;
import loyaltycard.repository.entity.TransactionEntity;
import loyaltycard.repository.entity.TransactionStatusEntity;
import loyaltycard.service.model.Direction;
import loyaltycard.service.model.Transaction;
import loyaltycard.service.model.TransactionStatus;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toDomain(TransactionEntity transactionEntity) {

        Transaction transaction = new Transaction();
        transaction.setId(transactionEntity.getId());
        transaction.setAdminId(transactionEntity.getAdminId());
        transaction.setCardId(transactionEntity.getCardId());
        transaction.setDirection(Direction.valueOf(transactionEntity.getDirection().name()));
        transaction.setAmount(transactionEntity.getAmount());
        transaction.setStatus(TransactionStatus.valueOf(transactionEntity.getStatus().name()));
        transaction.setNote(transactionEntity.getNote());
        transaction.setCreatedAt(transactionEntity.getCreatedAt());
        transaction.setUpdatedAt(transactionEntity.getUpdatedAt());

        return transaction;
    }

    public TransactionDto toDto(Transaction transaction) {

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setId(transaction.getId());
        transactionDto.setAdminId(transaction.getAdminId());
        transactionDto.setCardId(transaction.getCardId());
        transactionDto.setDirection(DirectionDto.valueOf(transaction.getDirection().name()));
        transactionDto.setAmount(transaction.getAmount());
        transactionDto.setStatus(TransactionStatusDto.valueOf(transaction.getStatus().name()));
        transactionDto.setNote(transaction.getNote());
        transactionDto.setCreatedAt(transaction.getCreatedAt());
        transactionDto.setUpdatedAt(transaction.getUpdatedAt());

        return transactionDto;
    }

    public TransactionEntity toEntity(Transaction transaction) {

        TransactionEntity transactionEntity = new TransactionEntity();
        transactionEntity.setId(transaction.getId());
        transactionEntity.setAdminId(transaction.getAdminId());
        transactionEntity.setCardId(transaction.getCardId());
        transactionEntity.setDirection(DirectionEntity.valueOf(transaction.getDirection().name()));
        transactionEntity.setAmount(transaction.getAmount());
        transactionEntity.setStatus(TransactionStatusEntity.valueOf(transaction.getStatus().name()));
        transactionEntity.setNote(transaction.getNote());
        transactionEntity.setCreatedAt(transaction.getCreatedAt());
        transactionEntity.setUpdatedAt(transaction.getUpdatedAt());

        return transactionEntity;
    }
}
