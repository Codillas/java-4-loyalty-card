package loyaltycard.service;

import loyaltycard.service.model.Transaction;

import java.util.List;
import java.util.UUID;

public interface TransactionService {

    Transaction createTransaction(UUID cardId, Transaction transaction, UUID adminId);

    List<Transaction> getTransactions(UUID cardId);

    Transaction getTransactionById(UUID cardId, UUID transactionId);

    Transaction cancelTransaction(UUID cardId, UUID transactionId);
}
